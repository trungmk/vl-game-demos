import SwiftUI

// Pink/info tinted banner used at bottom of cards (c4 "Credentials encrypted",
// c1 "Read-only view", etc.). Icon left + body text right.

struct VLFooterBanner: View {
    let icon: String
    let text: String
    var tone: VLDebugBoxTone = .info

    var body: some View {
        HStack(alignment: .top, spacing: VLSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(tone.titleColor)
            Text(text)
                .font(VLFont.bodySm)
                .foregroundColor(tone.bodyColor)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(VLSpacing.md)
        .background(tone.background)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(tone.outline, lineWidth: 1)
        )
    }
}
