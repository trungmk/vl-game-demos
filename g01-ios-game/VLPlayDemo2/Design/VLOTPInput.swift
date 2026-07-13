import SwiftUI

// 6-digit OTP grid with active border + countdown chip.
// Dùng ở c5 Device Auth.

struct VLOTPInput: View {
    @Binding var code: String
    var length: Int = 6
    @FocusState private var focused: Bool

    var body: some View {
        ZStack {
            HStack(spacing: VLSpacing.sm) {
                ForEach(0..<length, id: \.self) { idx in
                    digitBox(at: idx)
                }
            }
            // Hidden text field captures input
            TextField("", text: $code)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused($focused)
                .opacity(0.001)
                .frame(maxWidth: .infinity)
                .onChange(of: code) { newValue in
                    let digits = newValue.filter(\.isNumber)
                    if digits.count <= length {
                        code = digits
                    } else {
                        code = String(digits.prefix(length))
                    }
                }
        }
        .contentShape(Rectangle())
        .onTapGesture { focused = true }
        .onAppear { focused = true }
    }

    private func digitBox(at index: Int) -> some View {
        let chars = Array(code)
        let value: String = index < chars.count ? String(chars[index]) : ""
        let isActive = index == code.count
        let isFilled = !value.isEmpty

        return Text(value)
            .font(VLFont.h2)
            .foregroundColor(VLColor.onSurface)
            .frame(width: 44, height: 52)
            .background(VLColor.surfaceContainerLowest)
            .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                    .stroke(isActive || isFilled ? VLColor.primary : VLColor.outlineVariant,
                            lineWidth: isActive ? 2 : 1)
            )
    }
}

struct VLCountdownChip: View {
    let secondsRemaining: Int
    var body: some View {
        let mins = secondsRemaining / 60
        let secs = secondsRemaining % 60
        return HStack(spacing: 4) {
            Image(systemName: VLIcon.clock)
                .font(.system(size: 11, weight: .medium))
            Text(String(format: "%02d:%02d", mins, secs))
                .font(VLFont.code)
        }
        .foregroundColor(VLColor.onSurfaceVariant)
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(VLColor.surfaceContainer)
        .clipShape(Capsule())
    }
}
