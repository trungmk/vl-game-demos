import SwiftUI

// Square card tile used in b1 Main Hub grid.
// Icon top + label bottom, surface-container-lowest background, 1px outline.

struct VLActionTile: View {
    let icon: String
    let label: String
    var tint: Color = VLColor.primary
    var enabled: Bool = true
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: VLSpacing.sm) {
                ZStack {
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .fill(tint.opacity(0.10))
                        .frame(width: 44, height: 44)
                    Image(systemName: icon)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(tint)
                }
                Text(label)
                    .font(VLFont.labelMd)
                    .foregroundColor(VLColor.onSurface)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
                    .frame(height: 32, alignment: .top)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, VLSpacing.md)
            .padding(.horizontal, VLSpacing.sm)
            .background(VLColor.surfaceContainerLowest)
            .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                    .stroke(VLColor.outlineVariant, lineWidth: 1)
            )
            .opacity(enabled ? 1 : 0.45)
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}
