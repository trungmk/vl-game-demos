import SwiftUI

// Material 3 button styles per design system.
//   .primary     → filled (red bc001f, white text) — main CTA
//   .secondary   → outlined (red border + red text on white) — secondary CTA
//   .destructive → filled (error bc001f, white text) — Delete / Force Kick
//   .ghost       → text only (mutedText) — Cancel / textual link
//   .neutral     → filled (secondary-container gray) — "Chơi Ngay (Guest)"

enum VLButtonStyleKind { case primary, secondary, destructive, ghost, neutral }

struct VLButtonStyle: ButtonStyle {
    var kind: VLButtonStyleKind = .primary
    var fullWidth: Bool = true
    var compact: Bool = false

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(VLFont.button)
            .foregroundColor(foreground)
            .frame(maxWidth: fullWidth ? .infinity : nil, minHeight: compact ? 36 : 48)
            .padding(.horizontal, compact ? 14 : 20)
            .background(background)
            .overlay(overlay)
            .clipShape(Capsule())
            .opacity(configuration.isPressed ? 0.78 : 1)
            .scaleEffect(configuration.isPressed ? 0.985 : 1)
            .animation(.easeInOut(duration: 0.12), value: configuration.isPressed)
    }

    private var foreground: Color {
        switch kind {
        case .primary:     return VLColor.onPrimary
        case .secondary:   return VLColor.primary
        case .destructive: return VLColor.onError
        case .ghost:       return VLColor.onSurfaceVariant
        case .neutral:     return VLColor.onSecondaryContainer
        }
    }

    private var background: some View {
        Group {
            switch kind {
            case .primary:     VLColor.primary
            case .secondary:   VLColor.surfaceContainerLowest
            case .destructive: VLColor.error
            case .ghost:       Color.clear
            case .neutral:     VLColor.secondaryContainer
            }
        }
    }

    private var overlay: some View {
        Group {
            if kind == .secondary {
                Capsule().stroke(VLColor.primary, lineWidth: 1.2)
            } else if kind == .ghost {
                Capsule().stroke(VLColor.outlineVariant, lineWidth: 1)
            }
        }
    }
}

extension View {
    func vlButton(_ kind: VLButtonStyleKind = .primary,
                  fullWidth: Bool = true,
                  compact: Bool = false) -> some View {
        self.buttonStyle(VLButtonStyle(kind: kind, fullWidth: fullWidth, compact: compact))
    }
}
