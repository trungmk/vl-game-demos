import SwiftUI

// Read-only view of the CMS-driven SDK config (`GET /api/v1/sdk/config?clientId=...`).
// Calls VLPlaySDKManager public API (currentConfig + isFeatureEnabled + enabledPaymentMethods + support helpers).

struct RemoteConfigView: View {
    @State private var snapshotDict: [String: Any]? = nil
    @State private var ready: Bool = false

    var body: some View {
        List {
            Section("Status") {
                row(label: "Loaded", value: ready ? "YES" : "NO")
                row(label: "ClientID", value: clientId())
                row(label: "SDK min version", value: (VLPlaySDKManager.sdkMinVersion() ?? ""))
            }

            Section("Features (CMS toggles)") {
                featureRow(name: "Identity Verification", key: VLPlaySDKFeatureIdentityVerification)
                featureRow(name: "Anti-Addiction",        key: VLPlaySDKFeatureAntiAddiction)
                featureRow(name: "Guest Login",           key: VLPlaySDKFeatureGuestLogin)
                featureRow(name: "AppsFlyer Tracking",    key: VLPlaySDKFeatureAppsFlyerTracking)
                featureRow(name: "OTP Required",          key: VLPlaySDKFeatureOTPRequired)
            }

            Section("Payment Methods") {
                paymentRow(name: "Apple IAP",   id: VLPlaySDKPaymentMethodAppleIAP)
                paymentRow(name: "Google Play", id: VLPlaySDKPaymentMethodGooglePlay)
                paymentRow(name: "AppotaPay",   id: VLPlaySDKPaymentMethodAppotaPay)
                paymentRow(name: "External",    id: VLPlaySDKPaymentMethodExternal)
            }

            Section("Support") {
                contactRow(label: "Hotline",  value: (VLPlaySDKManager.supportHotline() ?? ""),      action: callHotline)
                contactRow(label: "Email",    value: (VLPlaySDKManager.supportEmailSupport() ?? ""), action: openEmail)
                contactRow(label: "Fanpage",  value: (VLPlaySDKManager.supportFanpage() ?? ""),      action: openFanpage)
            }

            if let dict = snapshotDict {
                Section("Raw snapshot (parsed)") {
                    let pretty = prettyJSON(dict)
                    Text(pretty)
                        .font(VLFont.code)
                        .foregroundColor(VLColor.onSurfaceVariant)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .textSelection(.enabled)
                }
            }
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background(VLColor.surface)
        .navigationTitle("Remote Config")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            Button {
                refresh()
            } label: {
                Image(systemName: VLIcon.refresh)
            }
        }
        .onAppear { refresh() }
    }

    private func refresh() {
        ready = VLPlaySDKManager.isConfigReady()
        snapshotDict = VLPlaySDKManager.currentConfig() as? [String: Any]
    }

    private func row(label: String, value: String) -> some View {
        HStack {
            Text(label).font(VLFont.bodyMd).foregroundColor(VLColor.onSurfaceVariant)
            Spacer()
            Text(value.isEmpty ? "—" : value)
                .font(VLFont.bodyMd.weight(.medium))
                .foregroundColor(VLColor.onSurface)
                .multilineTextAlignment(.trailing)
        }
    }

    private func featureRow(name: String, key: String) -> some View {
        let enabled = VLPlaySDKManager.isFeatureEnabled(key)
        return HStack {
            Image(systemName: enabled ? VLIcon.success : "circle")
                .foregroundColor(enabled ? VLColor.success : VLColor.outlineVariant)
            Text(name).font(VLFont.bodyMd).foregroundColor(VLColor.onSurface)
            Spacer()
            Text(enabled ? "ON" : "OFF")
                .font(VLFont.bodySm.weight(.bold))
                .foregroundColor(enabled ? VLColor.success : VLColor.onSurfaceVariant)
        }
    }

    private func paymentRow(name: String, id: String) -> some View {
        let enabled = VLPlaySDKManager.isPaymentMethodEnabled(id)
        return HStack {
            Image(systemName: enabled ? VLIcon.success : "circle")
                .foregroundColor(enabled ? VLColor.success : VLColor.outlineVariant)
            Text(name).font(VLFont.bodyMd).foregroundColor(VLColor.onSurface)
            Spacer()
            Text(id).font(VLFont.code).foregroundColor(VLColor.onSurfaceVariant)
        }
    }

    private func contactRow(label: String, value: String, action: @escaping () -> Void) -> some View {
        HStack {
            Text(label).font(VLFont.bodyMd).foregroundColor(VLColor.onSurfaceVariant)
            Spacer()
            if value.isEmpty {
                Text("—").font(VLFont.bodyMd).foregroundColor(VLColor.onSurfaceVariant)
            } else {
                Button(action: action) {
                    Text(value)
                        .font(VLFont.bodyMd.weight(.medium))
                        .foregroundColor(VLColor.primary)
                        .multilineTextAlignment(.trailing)
                }
            }
        }
    }

    private func callHotline() {
        let raw = (VLPlaySDKManager.supportHotline() ?? "") ?? ""
        let digits = raw.filter { "0123456789+".contains($0) }
        guard !digits.isEmpty, let url = URL(string: "tel://\(digits)") else { return }
        UIApplication.shared.open(url)
    }

    private func openEmail() {
        let addr = (VLPlaySDKManager.supportEmailSupport() ?? "") ?? ""
        guard !addr.isEmpty, let url = URL(string: "mailto:\(addr)") else { return }
        UIApplication.shared.open(url)
    }

    private func openFanpage() {
        let raw = (VLPlaySDKManager.supportFanpage() ?? "") ?? ""
        guard !raw.isEmpty else { return }
        let urlStr = raw.hasPrefix("http") ? raw : "https://\(raw)"
        guard let url = URL(string: urlStr) else { return }
        UIApplication.shared.open(url)
    }

    private func clientId() -> String {
        guard let path = Bundle.main.path(forResource: "VLPlaySDK-Info", ofType: "plist"),
              let dict = NSDictionary(contentsOfFile: path) as? [String: Any],
              let id = dict["VLPLAY_APP_ID"] as? String else {
            return "—"
        }
        return id
    }

    private func prettyJSON(_ dict: [String: Any]) -> String {
        guard JSONSerialization.isValidJSONObject(dict),
              let data = try? JSONSerialization.data(withJSONObject: dict, options: [.prettyPrinted, .sortedKeys]),
              let s = String(data: data, encoding: .utf8) else {
            return "(invalid)"
        }
        return s
    }
}
