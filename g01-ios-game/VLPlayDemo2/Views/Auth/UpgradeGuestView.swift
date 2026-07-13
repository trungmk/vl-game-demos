import SwiftUI
import VLPlaySDK

// c4 — Upgrade Guest → real account.

struct UpgradeGuestView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var loading: VLLoadingCenter

    @State private var username = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var email = ""
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    HStack {
                        Text("Nâng cấp tài khoản")
                            .font(VLFont.h1)
                            .foregroundColor(VLColor.onSurface)
                        VLStatePill(text: "GUEST", tone: .tertiary)
                    }

                    Text("Đặt username + password cho phiên Guest hiện tại. Tiến trình chơi được giữ nguyên.")
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurfaceVariant)

                    field(icon: VLIcon.person, label: "Username",
                          hint: "8–12 ký tự, chỉ chữ thường và số",
                          value: $username, secure: false)
                    field(icon: VLIcon.lock,   label: "Password",
                          hint: "Tối thiểu 8 ký tự",
                          value: $password, secure: true)
                    field(icon: VLIcon.lockRotate, label: "Confirm Password",
                          hint: nil, value: $confirmPassword, secure: true)
                    field(icon: VLIcon.email,  label: "Email (tuỳ chọn)",
                          hint: nil, value: $email, secure: false)

                    Button(action: submit) {
                        HStack(spacing: 6) {
                            Text("Nâng cấp tài khoản")
                            Image(systemName: VLIcon.arrowRight)
                        }
                    }
                    .vlButton(.primary)
                    .disabled(!canSubmit)
                    .opacity(canSubmit ? 1 : 0.55)
                    .padding(.top, VLSpacing.sm)

                    Button("Huỷ") { dismiss() }
                        .vlButton(.ghost)
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
    }

    private var canSubmit: Bool {
        usernameValid && passwordValid && password == confirmPassword
    }

    private func field(icon: String, label: String, hint: String?,
                       value: Binding<String>, secure: Bool) -> some View {
        VStack(alignment: .leading, spacing: VLSpacing.xs) {
            Text(label)
                .font(VLFont.labelMd)
                .foregroundColor(VLColor.onSurfaceVariant)

            HStack(spacing: VLSpacing.sm) {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(VLColor.onSurfaceVariant)
                Group {
                    if secure {
                        SecureField(label, text: value)
                    } else {
                        TextField(label, text: value)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                    }
                }
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurface)
            }
            .padding(.horizontal, VLSpacing.md)
            .frame(height: 48)
            .background(VLColor.surfaceContainerLowest)
            .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                    .stroke(VLColor.outlineVariant, lineWidth: 1)
            )

            if let hint {
                Text(hint)
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
    }

    /// Username regex per BE Joi (openapi v3.1 GuestUpgradeRequest):
    /// 8–12 chars, lowercase alphanumeric only.
    private static let usernameRegex = try? NSRegularExpression(pattern: "^[a-z0-9]{8,12}$")

    private var usernameValid: Bool {
        guard let r = Self.usernameRegex else { return false }
        let range = NSRange(username.startIndex..., in: username)
        return r.firstMatch(in: username, range: range) != nil
    }

    private var passwordValid: Bool { password.count >= 8 && password.count <= 255 }

    private func submit() {
        guard usernameValid else {
            toast.error("Username không hợp lệ", "8–12 ký tự, chỉ chữ thường và số")
            return
        }
        guard passwordValid else {
            toast.error("Password không hợp lệ", "Tối thiểu 8 ký tự")
            return
        }
        guard password == confirmPassword else {
            toast.error("Validation", "Confirm password không khớp")
            return
        }
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        loading.show("Đang nâng cấp…")
        VLPlaySDKManager.default().upgradeGuestAccount(withUsername: username,
                                                 password: password,
                                                 email: trimmedEmail) { status, error in
            DispatchQueue.main.async {
                self.loading.hide()
                if status {
                    self.toast.success("Upgrade thành công",
                                       "Guest đã thành tài khoản @\(self.username)")
                    // VIDUser is updated by SDK; bounce SDKEnvironment to re-render UI
                    self.env.objectWillChange.send()
                    self.dismiss()
                } else {
                    let msg = Self.errorMessage(error, fallback: "Upgrade thất bại")
                    self.toast.error("Upgrade thất bại", msg)
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
