import SwiftUI

// Status pill — full pill shape, color tied to semantic state.
// Used: Guest/Linked/Verified/Failed/Pending/Most Popular/SUCCESS/STAGING etc.

enum VLPillTone {
    case neutral, primary, tertiary, success, warning, error, secondary, accent

    var background: Color {
        switch self {
        case .neutral:   return VLColor.secondaryContainer
        case .primary:   return VLColor.primaryContainer
        case .tertiary:  return VLColor.tertiaryContainer
        case .success:   return VLColor.successContainer
        case .warning:   return VLColor.warningContainer
        case .error:     return VLColor.errorContainer
        case .secondary: return VLColor.surfaceContainerHigh
        case .accent:    return VLColor.tertiary
        }
    }

    var foreground: Color {
        switch self {
        case .neutral:   return VLColor.onSecondaryContainer
        case .primary:   return VLColor.onPrimaryContainer
        case .tertiary:  return VLColor.onTertiaryContainer
        case .success:   return Color(hex: 0x1B5E20)
        case .warning:   return Color(hex: 0xB35400)
        case .error:     return VLColor.onErrorContainer
        case .secondary: return VLColor.onSurfaceVariant
        case .accent:    return Color.white
        }
    }
}

struct VLStatePill: View {
    let text: String
    var tone: VLPillTone = .neutral
    var icon: String? = nil

    var body: some View {
        HStack(spacing: 4) {
            if let icon {
                Image(systemName: icon)
                    .font(.system(size: 10, weight: .bold))
            }
            Text(text)
                .font(VLFont.labelBold)
        }
        .foregroundColor(tone.foreground)
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(tone.background)
        .clipShape(Capsule())
    }
}
