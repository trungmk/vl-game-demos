import SwiftUI

// Country-code dropdown + phone input + Send OTP button (c5).

struct VLPhoneInputRow: View {
    @Binding var countryCode: String
    @Binding var phone: String
    var onSendOTP: () -> Void
    var sendDisabled: Bool = false

    private let codes = ["+84", "+1", "+44", "+86", "+81", "+82"]

    var body: some View {
        HStack(spacing: VLSpacing.sm) {
            Menu {
                ForEach(codes, id: \.self) { code in
                    Button(code) { countryCode = code }
                }
            } label: {
                HStack(spacing: 4) {
                    Text(countryCode)
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurface)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
                .padding(.horizontal, 10)
                .frame(height: 44)
                .background(VLColor.surfaceContainerLowest)
                .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .stroke(VLColor.outlineVariant, lineWidth: 1)
                )
            }

            TextField("Phone number", text: $phone)
                .keyboardType(.phonePad)
                .foregroundColor(VLColor.onSurface)
                .padding(.horizontal, 12)
                .frame(height: 44)
                .background(VLColor.surfaceContainerLowest)
                .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .stroke(VLColor.outlineVariant, lineWidth: 1)
                )

            Button("Send OTP", action: onSendOTP)
                .vlButton(.secondary, fullWidth: false, compact: true)
                .disabled(sendDisabled)
                .opacity(sendDisabled ? 0.55 : 1)
        }
    }
}
