import SwiftUI
import VLPlaySDK

// AppsFlyer Events test console — fire any CMS-configured AppsFlyer event
// through the SDK's dynamic resolver.
//
// Reads `afEvents` from VLPlaySDKManager.currentConfig() (top-level sibling of
// `features` in /sdk/config). The event list is whatever Ops put in
// afEvents.map (stableKey → afName) plus afEvents.disabled[]. Firing passes
// the STABLE KEY so the SDK's resolver does the rename / drop — exactly the
// runtime path a production game's events take (also honors the
// features.appsFlyerTracking master toggle). When CMS hasn't configured
// afEvents yet, falls back to a built-in canonical AppsFlyer event seed so
// the console still works.
//
// Parity Android demo [AppsFlyerEventsScreen]. Reached from the MainHub tile.

private struct AFEventItem: Identifiable {
    let stableKey: String   // resolver input — what we fire
    let afName: String      // resolver output — CMS-mapped name (== stableKey if no remap)
    let disabled: Bool      // in afEvents.disabled → resolver drops it before dispatch
    let fromCMS: Bool       // false = built-in fallback seed (CMS afEvents empty)
    var id: String { stableKey }
}

private struct AFParamRow: Identifiable {
    let id = UUID()
    var key: String
    var value: String
}

struct AppsFlyerEventsView: View {
    @EnvironmentObject var toast: VLToastCenter

    @State private var events: [AFEventItem] = []
    @State private var afEventsEnabled = false
    @State private var trackingEnabled = false
    @State private var usingFallback = false
    @State private var selectedKey: String? = nil
    @State private var params: [AFParamRow] = []
    @State private var lastFired: String? = nil

    var body: some View {
        ScrollView {
            VStack(spacing: VLSpacing.md) {
                statusCard
                eventListCard
                if let key = selectedKey,
                   let ev = events.first(where: { $0.stableKey == key }) {
                    paramEditorCard(for: ev)
                    sendButton(for: ev)
                }
                if let last = lastFired { lastFiredCard(last) }
            }
            .padding(.horizontal, VLSpacing.safeMargin)
            .padding(.vertical, VLSpacing.md)
        }
        .background(VLColor.surface.ignoresSafeArea())
        .navigationTitle("AppsFlyer Events")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            Button { loadConfig() } label: {
                Image(systemName: VLIcon.refresh)
            }
        }
        .onAppear { loadConfig() }
    }

    // MARK: - Status

    private var statusCard: some View {
        card(title: "TRẠNG THÁI") {
            statusRow(label: "afEvents layer (CMS)",
                      value: afEventsEnabled ? "ON" : "OFF",
                      ok: afEventsEnabled)
            divider
            statusRow(label: "appsFlyerTracking (master)",
                      value: trackingEnabled ? "ON" : "OFF",
                      ok: trackingEnabled)
            divider
            statusRow(label: "Số event",
                      value: "\(events.count)\(usingFallback ? " (built-in)" : " (CMS)")",
                      ok: !usingFallback)
            if usingFallback {
                Text("CMS afEvents trống → dùng danh sách mẫu built-in. Cấu hình afEvents trong CMS để thấy event thật.")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else if !trackingEnabled {
                Text("Master tracking OFF → SDK short-circuit, event sẽ không thực sự gửi đi.")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.warning)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }

    private func statusRow(label: String, value: String, ok: Bool) -> some View {
        HStack {
            Text(label).font(VLFont.bodyMd).foregroundColor(VLColor.onSurfaceVariant)
            Spacer()
            Text(value)
                .font(VLFont.bodySm.weight(.bold))
                .foregroundColor(ok ? VLColor.success : VLColor.onSurfaceVariant)
        }
    }

    // MARK: - Event list (picker)

    private var eventListCard: some View {
        card(title: "CHỌN EVENT") {
            if events.isEmpty {
                Text("Không có event nào.")
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                ForEach(events) { ev in
                    eventRow(ev)
                    if ev.id != events.last?.id { divider }
                }
            }
        }
    }

    private func eventRow(_ ev: AFEventItem) -> some View {
        let isSelected = ev.stableKey == selectedKey
        return Button {
            selectedKey = ev.stableKey
            params = Self.defaultParams(for: ev.stableKey)
        } label: {
            HStack(spacing: VLSpacing.sm) {
                Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                    .foregroundColor(isSelected ? VLColor.primary : VLColor.outlineVariant)
                VStack(alignment: .leading, spacing: 2) {
                    Text(ev.stableKey)
                        .font(VLFont.codeMd)
                        .foregroundColor(VLColor.onSurface)
                    if ev.afName != ev.stableKey {
                        Text("→ \(ev.afName)")
                            .font(VLFont.code)
                            .foregroundColor(VLColor.accent)
                    }
                }
                Spacer()
                if ev.disabled {
                    Text("DISABLED")
                        .font(VLFont.labelBold)
                        .foregroundColor(VLColor.error)
                        .padding(.horizontal, 8).padding(.vertical, 3)
                        .background(VLColor.errorContainer)
                        .clipShape(Capsule())
                }
            }
            .padding(.vertical, VLSpacing.xs)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    // MARK: - Param editor

    private func paramEditorCard(for ev: AFEventItem) -> some View {
        card(title: "PARAMS — \(ev.stableKey)") {
            if params.isEmpty {
                Text("Chưa có param. Bấm “+ Thêm param”.")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            ForEach($params) { $row in
                HStack(spacing: VLSpacing.sm) {
                    TextField("key", text: $row.key)
                        .font(VLFont.code)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                        .padding(8)
                        .background(VLColor.surfaceContainerLow)
                        .clipShape(RoundedRectangle(cornerRadius: VLRadius.sm, style: .continuous))
                    TextField("value", text: $row.value)
                        .font(VLFont.code)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                        .padding(8)
                        .background(VLColor.surfaceContainerLow)
                        .clipShape(RoundedRectangle(cornerRadius: VLRadius.sm, style: .continuous))
                    Button {
                        params.removeAll { $0.id == row.id }
                    } label: {
                        Image(systemName: "minus.circle.fill")
                            .foregroundColor(VLColor.error)
                    }
                    .buttonStyle(.plain)
                }
            }
            HStack(spacing: VLSpacing.md) {
                Button {
                    params.append(AFParamRow(key: "", value: ""))
                } label: {
                    Label("Thêm param", systemImage: "plus.circle")
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.primary)
                }
                .buttonStyle(.plain)
                Spacer()
                Button {
                    params = Self.defaultParams(for: ev.stableKey)
                } label: {
                    Label("Mặc định", systemImage: VLIcon.refresh)
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
                .buttonStyle(.plain)
            }
            .padding(.top, VLSpacing.xs)
        }
    }

    // MARK: - Send

    private func sendButton(for ev: AFEventItem) -> some View {
        let title = ev.disabled ? "Gửi (sẽ bị drop)" : "Gửi event"
        return Button(title) { send(ev) }
            .vlButton(ev.disabled ? .secondary : .primary)
    }

    private func lastFiredCard(_ text: String) -> some View {
        card(title: "LẦN GỬI GẦN NHẤT") {
            Text(text)
                .font(VLFont.code)
                .foregroundColor(VLColor.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .leading)
                .textSelection(.enabled)
        }
    }

    // MARK: - Logic

    private func send(_ ev: AFEventItem) {
        let values = buildValues()
        // Fire the STABLE KEY — the SDK resolver remaps to afName / drops if
        // disabled / short-circuits if the master toggle is off.
        VLPlaySDKManager.default().logEvent(ev.stableKey, withValues: values)

        let paramDesc = values.isEmpty ? "(no params)"
            : values.map { "\($0.key)=\($0.value)" }.sorted().joined(separator: ", ")
        if ev.disabled {
            lastFired = "\(ev.stableKey) → DROPPED (disabled in CMS) · \(paramDesc)"
            toast.warning("Event disabled", "\(ev.stableKey) bị disable trong CMS → resolver drop, không gửi.")
        } else if !trackingEnabled {
            lastFired = "\(ev.stableKey) → \(ev.afName) · master OFF · \(paramDesc)"
            toast.warning("Tracking OFF", "features.appsFlyerTracking = OFF → SDK short-circuit.")
        } else {
            lastFired = "\(ev.stableKey) → \(ev.afName) · \(paramDesc)"
            toast.success("Đã fire", "\(ev.stableKey) → \(ev.afName)")
        }
    }

    private func buildValues() -> [String: Any] {
        var dict: [String: Any] = [:]
        for row in params {
            let k = row.key.trimmingCharacters(in: .whitespaces)
            guard !k.isEmpty else { continue }
            dict[k] = Self.coerce(row.value)
        }
        return dict
    }

    private func loadConfig() {
        trackingEnabled = VLPlaySDKManager.isFeatureEnabled(VLPlaySDKFeatureAppsFlyerTracking)

        var map: [String: String] = [:]
        var disabled: [String] = []
        var enabled = false
        if let cfg = VLPlaySDKManager.currentConfig() as? [String: Any],
           let af = cfg["afEvents"] as? [String: Any] {
            enabled = (af["enabled"] as? Bool) ?? false
            if let rawMap = af["map"] as? [String: Any] {
                for (k, v) in rawMap { if let s = v as? String { map[k] = s } }
            }
            if let rawDisabled = af["disabled"] as? [Any] {
                disabled = rawDisabled.compactMap { $0 as? String }
            }
        }
        afEventsEnabled = enabled

        var items: [AFEventItem] = []
        var seen = Set<String>()
        for (k, v) in map.sorted(by: { $0.key < $1.key }) {
            items.append(AFEventItem(stableKey: k, afName: v,
                                     disabled: disabled.contains(k), fromCMS: true))
            seen.insert(k)
        }
        for d in disabled.sorted() where !seen.contains(d) {
            items.append(AFEventItem(stableKey: d, afName: d, disabled: true, fromCMS: true))
            seen.insert(d)
        }

        if items.isEmpty {
            usingFallback = true
            items = Self.fallbackEvents.map {
                AFEventItem(stableKey: $0, afName: $0, disabled: false, fromCMS: false)
            }
        } else {
            usingFallback = false
        }
        events = items

        // Drop a stale selection that no longer exists after refresh.
        if let key = selectedKey, !items.contains(where: { $0.stableKey == key }) {
            selectedKey = nil
            params = []
        }
    }

    // MARK: - Helpers

    /// String → number coercion so af_revenue=100000 sends as a number not "100000".
    private static func coerce(_ s: String) -> Any {
        let t = s.trimmingCharacters(in: .whitespaces)
        if let i = Int(t) { return i }
        if let d = Double(t) { return d }
        return s
    }

    /// Recommended default params per common AppsFlyer event; generic single
    /// row for anything else. All editable / removable in the UI.
    private static func defaultParams(for key: String) -> [AFParamRow] {
        switch key {
        case "af_purchase", "af_payment_success":
            return [AFParamRow(key: "af_revenue", value: "100000"),
                    AFParamRow(key: "af_currency", value: "VND"),
                    AFParamRow(key: "af_content_id", value: "pack_001"),
                    AFParamRow(key: "af_quantity", value: "1")]
        case "af_add_to_cart", "af_initiated_checkout":
            return [AFParamRow(key: "af_revenue", value: "100000"),
                    AFParamRow(key: "af_currency", value: "VND"),
                    AFParamRow(key: "af_content_id", value: "pack_001")]
        case "af_login", "af_complete_registration":
            return [AFParamRow(key: "af_registration_method", value: "email")]
        case "af_content_view":
            return [AFParamRow(key: "af_content_id", value: "item_001"),
                    AFParamRow(key: "af_content_type", value: "product")]
        case "af_level_achieved":
            return [AFParamRow(key: "af_level", value: "2")]
        default:
            return [AFParamRow(key: "source", value: "demo_console")]
        }
    }

    private static let fallbackEvents = [
        "af_login",
        "af_complete_registration",
        "af_purchase",
        "af_content_view",
        "af_level_achieved",
        "af_add_to_cart",
        "af_initiated_checkout",
        "af_tutorial_completion",
    ]

    // MARK: - Card chrome

    @ViewBuilder
    private func card<Content: View>(title: String,
                                     @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            Text(title)
                .font(VLFont.labelBold)
                .foregroundColor(VLColor.onSurfaceVariant)
            content()
        }
        .padding(VLSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private var divider: some View {
        Rectangle()
            .fill(VLColor.outlineVariant.opacity(0.4))
            .frame(height: 1)
    }
}
