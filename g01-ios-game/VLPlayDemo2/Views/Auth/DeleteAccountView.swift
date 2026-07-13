import SwiftUI
import VLPlaySDK

// c6 — Delete Account confirm screen with type-DELETE.

struct DeleteAccountView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var loading: VLLoadingCenter
    @Environment(\.dismiss) private var dismiss

    @State private var typed: String = ""
    private let confirmKeyword = "DELETE"

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(spacing: VLSpacing.md) {
                    Image(systemName: VLIcon.warning)
                        .font(.system(size: 44, weight: .semibold))
                        .foregroundColor(VLColor.error)
                        .padding(.top, VLSpacing.md)

                    Text("Delete Account")
                        .font(VLFont.h1)
                        .foregroundColor(VLColor.onSurface)

                    Text("This action is permanent. Your session, payment records, and linked providers will be removed.")
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, VLSpacing.md)

                    VLDebugBox(
                        title: "AFFECTED RESOURCES",
                        lines: [
                            "• Active API Keys",
                            "• Staging Environments",
                            "• Historical request logs",
                            "• Linked OAuth providers"
                        ],
                        tone: .info
                    )

                    VStack(alignment: .leading, spacing: VLSpacing.xs) {
                        Text("Type \(confirmKeyword) to confirm")
                            .font(VLFont.labelMd)
                            .foregroundColor(VLColor.onSurfaceVariant)
                        TextField(confirmKeyword, text: $typed)
                            .font(VLFont.codeMd)
                            .foregroundColor(VLColor.onSurface)
                            .autocapitalization(.allCharacters)
                            .disableAutocorrection(true)
                            .padding(.horizontal, VLSpacing.md)
                            .frame(height: 48)
                            .background(VLColor.surfaceContainerLowest)
                            .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                                    .stroke(VLColor.error, lineWidth: 1.2)
                            )
                    }

                    Button("Deactivate Account", action: confirmDelete)
                        .vlButton(.destructive)
                        .disabled(typed != confirmKeyword)
                        .opacity(typed == confirmKeyword ? 1 : 0.55)

                    Button("Cancel") { dismiss() }
                        .vlButton(.secondary)
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
    }

    private func confirmDelete() {
        loading.show("Đang huỷ tài khoản…")
        VLPlaySDKManager.default().deactivateAccount { status, error in
            DispatchQueue.main.async {
                self.loading.hide()
                if status {
                    self.toast.success("Đã huỷ tài khoản", "Bạn sẽ được đăng xuất")
                    self.dismiss()
                } else {
                    let msg = Self.errorMessage(error, fallback: "Huỷ tài khoản thất bại")
                    self.toast.error("Lỗi", msg)
                }
            }
        }
    }

    private static func errorMessage(_ error: Error?, fallback: String) -> String {
        guard let nsErr = error as NSError? else { return fallback }
        if let msg = nsErr.userInfo["message"] as? String, !msg.isEmpty { return msg }
        let desc = nsErr.localizedDescription
        return desc.isEmpty ? fallback : desc
    }
}
