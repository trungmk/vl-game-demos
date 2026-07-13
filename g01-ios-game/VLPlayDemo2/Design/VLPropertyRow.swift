import SwiftUI

// Key-value split row. Value text-selectable for copy.
// Mockup c1 Profile, Profile card in b1.

struct VLPropertyRow: View {
    let label: String
    let value: String
    var icon: String? = nil
    var copyable: Bool = false

    var body: some View {
        HStack(spacing: VLSpacing.sm) {
            if let icon {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .frame(width: 20)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(VLFont.labelMd)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Text(value)
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurface)
                    .textSelection(.enabled)
            }
            Spacer(minLength: VLSpacing.sm)
            if copyable {
                Button {
                    UIPasteboard.general.string = value
                } label: {
                    Image(systemName: VLIcon.copy)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(VLColor.primary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, VLSpacing.xs)
    }
}
