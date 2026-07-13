import SwiftUI

// 16-character giftcode input — auto-format `XXXX-XXXX-XXXX-XXXX`.
// Used in f1 Giftcode Redeem.

struct VLGiftcodeInput: View {
    @Binding var rawCode: String  // store unformatted (digits/letters only, max 16)
    var maxLength: Int = 16
    var placeholder: String = "XXXX-XXXX-XXXX-XXXX"

    var body: some View {
        TextField(placeholder, text: Binding(
            get: { format(rawCode) },
            set: { newValue in
                let cleaned = newValue
                    .replacingOccurrences(of: "-", with: "")
                    .uppercased()
                    .filter { $0.isLetter || $0.isNumber }
                rawCode = String(cleaned.prefix(maxLength))
            }
        ))
        .font(VLFont.codeMd)
        .foregroundColor(VLColor.onSurface)
        .autocapitalization(.allCharacters)
        .disableAutocorrection(true)
        .padding(.horizontal, VLSpacing.md)
        .frame(height: 52)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.primary, lineWidth: 1.4)
        )
    }

    private func format(_ raw: String) -> String {
        var result = ""
        for (idx, char) in raw.enumerated() {
            if idx > 0 && idx % 4 == 0 { result.append("-") }
            result.append(char)
        }
        return result
    }
}
