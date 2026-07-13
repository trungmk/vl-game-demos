import SwiftUI
import VLPlaySDK

// f1 — Giftcode Redeem.
// Wires VLPlaySDKManager.redeemGiftcode → POST /api/v1/giftcode/redeem.
// Recent list persists across nav via NSUserDefaults.

struct GiftcodeRedeemView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var loading: VLLoadingCenter

    @State private var rawCode = ""
    @State private var recent: [RedeemEntry] = RedeemEntry.loadAll()
    @State private var lastResult: RedeemResult? = nil

    private let minLen = 4
    private let maxLen = 50

    private var isSignedIn: Bool { env.currentUser?.signedIn == true }
    private var canSubmit: Bool { isSignedIn && rawCode.count >= minLen }

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    Text("Đổi Giftcode")
                        .font(VLFont.h1)
                        .foregroundColor(VLColor.onSurface)
                    Text("Nhập mã \(minLen)–\(maxLen) ký tự để nhận quà.")
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurfaceVariant)

                    if !isSignedIn { signInGuard }
                    redeemCard
                    if let res = lastResult { resultCard(res) }
                    recentSection
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
    }

    // MARK: - Sign-in guard

    private var signInGuard: some View {
        HStack(spacing: VLSpacing.sm) {
            Image(systemName: VLIcon.warning)
                .foregroundColor(VLColor.warning)
            Text("Cần đăng nhập để đổi giftcode.")
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurface)
            Spacer()
        }
        .padding(VLSpacing.md)
        .background(VLColor.warningContainer.opacity(0.4))
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.warning.opacity(0.4), lineWidth: 1)
        )
    }

    // MARK: - Redeem input card

    private var redeemCard: some View {
        VStack(alignment: .leading, spacing: VLSpacing.md) {
            LinearGradient(colors: [Color(hex: 0xFF6B6B), Color(hex: 0xFFD93D), Color(hex: 0x6BCB77), Color(hex: 0x4D96FF)],
                           startPoint: .leading, endPoint: .trailing)
                .frame(height: 3)
                .clipShape(Capsule())

            Text("MÃ GIFTCODE")
                .font(VLFont.labelBold)
                .foregroundColor(VLColor.onSurfaceVariant)

            VLGiftcodeInput(rawCode: $rawCode, maxLength: maxLen, placeholder: "VD: TEST001")
            HStack(spacing: 4) {
                Image(systemName: "info.circle")
                    .font(.system(size: 11))
                Text("\(rawCode.count) / \(maxLen) ký tự")
                Spacer()
                if rawCode.count > 0 {
                    Button("Xoá") { rawCode = "" }
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.primary)
                }
            }
            .font(VLFont.bodySm)
            .foregroundColor(VLColor.onSurfaceVariant)

            Button(action: redeem) {
                HStack(spacing: 6) {
                    Image(systemName: VLIcon.download)
                    Text("Đổi quà")
                }
            }
            .vlButton(.primary)
            .disabled(!canSubmit)
            .opacity(canSubmit ? 1 : 0.55)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    // MARK: - Inline result card (per-attempt detail)

    @ViewBuilder
    private func resultCard(_ res: RedeemResult) -> some View {
        let (icon, tone, title): (String, Color, String) = {
            switch res.kind {
            case .success: return (VLIcon.success, VLColor.success, "Đổi mã thành công")
            case .failure: return (VLIcon.warning, VLColor.error,   "Đổi mã thất bại")
            }
        }()

        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack(spacing: VLSpacing.sm) {
                Image(systemName: icon)
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(tone)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(VLFont.labelBold)
                        .foregroundColor(VLColor.onSurface)
                    Text("Mã: \(res.code)")
                        .font(VLFont.codeMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
                Spacer()
                Button(action: { lastResult = nil }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
            }
            Text(res.message)
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurface)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(VLSpacing.md)
        .background(tone.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(tone.opacity(0.4), lineWidth: 1)
        )
    }

    // MARK: - Recent

    private var recentSection: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack {
                Text("Lịch sử gần đây")
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Spacer()
                if !recent.isEmpty {
                    Button("Xoá hết") {
                        recent = []
                        RedeemEntry.saveAll([])
                    }
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.primary)
                }
            }

            if recent.isEmpty {
                emptyRecent
            } else {
                recentList
            }
        }
    }

    private var emptyRecent: some View {
        VStack(spacing: VLSpacing.sm) {
            Image(systemName: VLIcon.archive)
                .font(.system(size: 28))
                .foregroundColor(VLColor.onSurfaceVariant)
            Text("Chưa có lượt đổi nào")
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
        .padding(VLSpacing.lg)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private var recentList: some View {
        VStack(spacing: 0) {
            ForEach(recent) { item in
                HStack(spacing: VLSpacing.sm) {
                    Image(systemName: item.status == .verified ? VLIcon.archive : VLIcon.warning)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(item.status == .verified ? VLColor.primary : VLColor.error)
                        .frame(width: 24)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.code)
                            .font(VLFont.codeMd)
                            .foregroundColor(VLColor.onSurface)
                            .lineLimit(1)
                            .truncationMode(.middle)
                        Text(item.datetime)
                            .font(VLFont.bodySm)
                            .foregroundColor(VLColor.onSurfaceVariant)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 2) {
                        Text(item.reward)
                            .font(VLFont.bodyMd)
                            .foregroundColor(VLColor.onSurface)
                            .lineLimit(1)
                        VLStatePill(text: item.status.pillText, tone: item.status == .failed ? .error : .neutral)
                    }
                }
                .padding(.vertical, VLSpacing.sm)
                if item.id != recent.last?.id {
                    Divider().padding(.leading, 36)
                }
            }
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    // MARK: - SDK call

    private func redeem() {
        guard canSubmit else { return }
        let code = rawCode
        loading.show("Đang đổi mã…")

        VLPlaySDKManager.default().redeemGiftcode(code) { status, data, error in
            DispatchQueue.main.async {
                self.loading.hide()
                if status {
                    let reward = Self.rewardLabel(from: data) ?? "Phần thưởng đã ghi nhận"
                    self.lastResult = RedeemResult(code: code, kind: .success,
                                                   message: "Mã \(code) đã được đổi. \(reward)")
                    self.appendRecent(.init(code: code, reward: reward,
                                            datetime: Self.nowString(), status: .verified))
                    self.toast.success("Đổi mã thành công", reward)
                    self.rawCode = ""
                } else {
                    let msg = Self.errorMessage(error, fallback: "Đổi mã thất bại")
                    self.lastResult = RedeemResult(code: code, kind: .failure, message: msg)
                    self.appendRecent(.init(code: code, reward: msg,
                                            datetime: Self.nowString(), status: .failed))
                    self.toast.error("Đổi mã thất bại", msg)
                }
            }
        }
    }

    private func appendRecent(_ entry: RedeemEntry) {
        recent.insert(entry, at: 0)
        if recent.count > 20 { recent = Array(recent.prefix(20)) }
        RedeemEntry.saveAll(recent)
    }

    // MARK: - Helpers

    private static func rewardLabel(from data: [AnyHashable: Any]?) -> String? {
        guard let d = data else { return nil }
        // BE response shape: { code, type, ...rewardFields } — surface what we get.
        if let r = d["reward"] as? String { return r }
        if let t = d["type"] as? String, !t.isEmpty {
            return "Loại: \(t)"
        }
        return nil
    }

    private static func errorMessage(_ error: Error?, fallback: String) -> String {
        guard let nsErr = error as NSError? else { return fallback }
        if let msg = nsErr.userInfo["message"] as? String, !msg.isEmpty { return msg }
        let desc = nsErr.localizedDescription
        return desc.isEmpty ? fallback : desc
    }

    private static func nowString() -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd HH:mm"
        return f.string(from: Date())
    }
}

// MARK: - Models

private struct RedeemResult {
    enum Kind { case success, failure }
    let code: String
    let kind: Kind
    let message: String
}

private struct RedeemEntry: Identifiable, Codable {
    var id = UUID()
    let code: String
    let reward: String
    let datetime: String
    let status: RedeemStatus

    private static let storeKey = "demo2.giftcode.recent"

    static func loadAll() -> [RedeemEntry] {
        guard let data = UserDefaults.standard.data(forKey: storeKey),
              let list = try? JSONDecoder().decode([RedeemEntry].self, from: data) else { return [] }
        return list
    }

    static func saveAll(_ list: [RedeemEntry]) {
        if let data = try? JSONEncoder().encode(list) {
            UserDefaults.standard.set(data, forKey: storeKey)
        }
    }
}

private enum RedeemStatus: String, Codable {
    case verified, failed
    var pillText: String { self == .verified ? "VERIFIED" : "FAILED" }
}
