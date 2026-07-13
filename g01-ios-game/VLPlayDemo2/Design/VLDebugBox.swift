import SwiftUI

// Pink/error tinted box, monospace content. Used for: purchaseCode in d3,
// Affected Resources in c6, demo control output in g1.

struct VLDebugBox: View {
    let title: String?
    let lines: [String]
    var tone: VLDebugBoxTone = .info

    var body: some View {
        VStack(alignment: .leading, spacing: VLSpacing.xs) {
            if let title {
                Text(title)
                    .font(VLFont.labelBold)
                    .foregroundColor(tone.titleColor)
            }
            ForEach(lines.indices, id: \.self) { idx in
                Text(lines[idx])
                    .font(VLFont.code)
                    .foregroundColor(tone.bodyColor)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .textSelection(.enabled)
            }
        }
        .padding(VLSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(tone.background)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(tone.outline, lineWidth: 1)
        )
    }
}

enum VLDebugBoxTone {
    case info, error, success

    var background: Color {
        switch self {
        case .info:    return VLColor.errorContainer.opacity(0.45)
        case .error:   return VLColor.errorContainer
        case .success: return VLColor.successContainer
        }
    }

    var outline: Color {
        switch self {
        case .info:    return VLColor.outlineVariant
        case .error:   return VLColor.error.opacity(0.4)
        case .success: return Color(hex: 0x4CAF50).opacity(0.4)
        }
    }

    var titleColor: Color {
        switch self {
        case .info:    return VLColor.onErrorContainer
        case .error:   return VLColor.error
        case .success: return Color(hex: 0x1B5E20)
        }
    }

    var bodyColor: Color {
        switch self {
        case .info, .error: return VLColor.onErrorContainer
        case .success:      return Color(hex: 0x1B5E20)
        }
    }
}
