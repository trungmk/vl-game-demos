import SwiftUI
import VLPlaySDK

// CCCD identity verification flow.
// Step 1: collect họ tên + 12-digit CCCD (+ 10-digit phone only if features.otpRequired ON)
// Step 2 (only if features.otpRequired ON): real SDK sendOTP → verifyOTP
// Step 3: success state — note: BE has no CCCD-storage endpoint yet, so this
//         demo treats OTP success as "verified" without persisting CCCD payload.
//         When BE ships PUT /api/v1/identity/verify, swap success path to call it.

struct IdentityVerificationView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var loading: VLLoadingCenter

    enum Step { case form, otp, done }

    @State private var step: Step = .form
    @State private var fullName: String = ""
    @State private var cccd: String = ""
    @State private var countryCode: String = "+84"
    @State private var phone: String = ""

    @State private var otpCode: String = ""
    @State private var resendCountdown: Int = 0
    @State private var resendTimer: Timer?

    private var otpRequired: Bool {
        VLPlaySDKManager.isFeatureEnabled(VLPlaySDKFeatureOTPRequired)
    }

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    Text("Xác minh CCCD")
                        .font(VLFont.h1)
                        .foregroundColor(VLColor.onSurface)
                    Text(otpRequired
                         ? "Điền thông tin và xác thực số điện thoại qua OTP."
                         : "Điền thông tin để xác minh tài khoản.")
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurfaceVariant)

                    switch step {
                    case .form:    formStep
                    case .otp:     otpStep
                    case .done:    doneStep
                    }
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
        .onDisappear { stopResendTimer() }
    }

    // MARK: - Step 1: form

    private var formStep: some View {
        VStack(alignment: .leading, spacing: VLSpacing.md) {
            field(title: "Họ và tên", placeholder: "Nguyễn Văn A", binding: $fullName, kbd: .default)
            field(title: "Số CCCD (12 chữ số)", placeholder: "012345678901", binding: $cccd, kbd: .numberPad, max: 12)

            if otpRequired {
                VStack(alignment: .leading, spacing: VLSpacing.xs) {
                    Text("Số điện thoại")
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                    VLPhoneInputRow(
                        countryCode: $countryCode,
                        phone: $phone,
                        onSendOTP: submitForm,
                        sendDisabled: !formValid
                    )
                }
            }

            Button(action: submitForm) {
                HStack(spacing: 6) {
                    Image(systemName: VLIcon.shieldOk)
                    Text(otpRequired ? "Gửi mã OTP" : "Xác minh ngay")
                }
            }
            .vlButton(.primary)
            .disabled(!formValid)
            .opacity(formValid ? 1 : 0.55)
            .padding(.top, VLSpacing.sm)
        }
    }

    private var formValid: Bool {
        let baseValid = !fullName.trimmingCharacters(in: .whitespaces).isEmpty
            && cccd.count == 12 && cccd.allSatisfy(\.isNumber)
        guard otpRequired else { return baseValid }
        return baseValid && phone.count == 10 && phone.allSatisfy(\.isNumber)
    }

    private func submitForm() {
        guard formValid else { return }
        if otpRequired {
            sendOTP()
        } else {
            // No OTP gate — treat form submission as verified
            step = .done
            toast.success("Đã ghi nhận", "Thông tin CCCD đã lưu (demo)")
        }
    }

    // MARK: - Step 2: OTP

    private var otpStep: some View {
        VStack(alignment: .leading, spacing: VLSpacing.md) {
            VLPropertyRow(label: "Họ tên", value: fullName, icon: VLIcon.person)
            VLPropertyRow(label: "CCCD",   value: cccd,     icon: VLIcon.card)
            VLPropertyRow(label: "Điện thoại", value: "\(countryCode)\(phone)", icon: VLIcon.phone)

            Divider().padding(.vertical, VLSpacing.xs)

            Text("Nhập mã OTP đã gửi đến \(countryCode)\(phone)")
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurfaceVariant)

            HStack { Spacer(); VLOTPInput(code: $otpCode); Spacer() }
                .padding(.vertical, VLSpacing.sm)

            HStack {
                Spacer()
                if resendCountdown > 0 {
                    VLCountdownChip(secondsRemaining: resendCountdown)
                } else {
                    Button("Gửi lại OTP", action: sendOTP)
                        .vlButton(.ghost)
                }
                Spacer()
            }

            Button(action: verifyOTP) {
                HStack(spacing: 6) {
                    Image(systemName: VLIcon.shieldOk)
                    Text("Xác thực & hoàn tất")
                }
            }
            .vlButton(.primary)
            .disabled(otpCode.count < 6)
            .opacity(otpCode.count < 6 ? 0.55 : 1)
            .padding(.top, VLSpacing.sm)

            Button("Quay lại sửa thông tin") {
                step = .form
                otpCode = ""
                stopResendTimer()
            }
            .vlButton(.ghost)
        }
    }

    private func sendOTP() {
        loading.show("Đang gửi OTP…")
        VLPlaySDKManager.default().sendOTP(toPhone: phone) { status, error in
            DispatchQueue.main.async {
                self.loading.hide()
                if status {
                    self.toast.success("OTP đã gửi", "đến \(self.countryCode)\(self.phone)")
                    self.step = .otp
                    self.startResendCountdown()
                } else {
                    self.toast.error("Lỗi gửi OTP", Self.errorMessage(error, fallback: "Không gửi được OTP"))
                }
            }
        }
    }

    private func verifyOTP() {
        loading.show("Đang xác thực…")
        VLPlaySDKManager.default().verifyOTP(withPhone: phone, otpCode: otpCode) { status, error in
            DispatchQueue.main.async {
                self.loading.hide()
                if status {
                    self.toast.success("Xác thực thành công", "CCCD đã được ghi nhận (demo)")
                    self.step = .done
                    self.stopResendTimer()
                } else {
                    self.toast.error("Lỗi xác thực", Self.errorMessage(error, fallback: "Mã OTP không đúng"))
                    self.otpCode = ""
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

    private func startResendCountdown() {
        resendCountdown = 60
        stopResendTimer()
        resendTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if resendCountdown > 0 { resendCountdown -= 1 } else { stopResendTimer() }
        }
    }

    private func stopResendTimer() {
        resendTimer?.invalidate(); resendTimer = nil
    }

    // MARK: - Step 3: done

    private var doneStep: some View {
        VStack(spacing: VLSpacing.md) {
            Image(systemName: VLIcon.success)
                .font(.system(size: 56))
                .foregroundColor(VLColor.success)
            Text("Xác minh thành công")
                .font(VLFont.h1)
                .foregroundColor(VLColor.onSurface)
            VStack(spacing: VLSpacing.xs) {
                VLPropertyRow(label: "Họ tên", value: fullName, icon: VLIcon.person)
                VLPropertyRow(label: "CCCD",   value: cccd,     icon: VLIcon.card)
                if otpRequired {
                    VLPropertyRow(label: "Điện thoại", value: "\(countryCode)\(phone)", icon: VLIcon.phone)
                }
            }
            .padding(VLSpacing.md)
            .background(VLColor.surfaceContainerLowest)
            .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))

            Text("Note: chưa có endpoint BE lưu CCCD — demo chỉ xác thực OTP. Sau khi PUT /identity/verify ship, payload sẽ được persist.")
                .font(VLFont.bodySm)
                .foregroundColor(VLColor.onSurfaceVariant)
                .multilineTextAlignment(.leading)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Helpers

    @ViewBuilder
    private func field(title: String, placeholder: String, binding: Binding<String>,
                       kbd: UIKeyboardType, max: Int? = nil) -> some View {
        VStack(alignment: .leading, spacing: VLSpacing.xs) {
            Text(title)
                .font(VLFont.labelMd)
                .foregroundColor(VLColor.onSurfaceVariant)
            TextField(placeholder, text: binding)
                .keyboardType(kbd)
                .padding(.horizontal, VLSpacing.md)
                .padding(.vertical, 10)
                .background(VLColor.surfaceContainerLow)
                .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .stroke(VLColor.outlineVariant, lineWidth: 1)
                )
                .onChange(of: binding.wrappedValue) { newVal in
                    if let max, newVal.count > max {
                        binding.wrappedValue = String(newVal.prefix(max))
                    }
                }
        }
    }
}
