import SwiftUI

// Top app bar — 2 variants per mockup:
//   .debugger : "SDK_DEBUGGER" + developer-mode icon (root tab screens, gray tone)
//   .brand    : "VLPlay SDK" + red diamond logo (detail screens)

enum VLTopBarVariant {
    case debugger
    case brand
}

struct VLTopAppBar<Trailing: View>: View {
    var variant: VLTopBarVariant = .debugger
    var subtitle: String? = nil
    var environmentBadge: String? = nil  // STAGING / PROD pill
    @ViewBuilder var trailing: () -> Trailing

    init(variant: VLTopBarVariant = .debugger,
         subtitle: String? = nil,
         environmentBadge: String? = nil,
         @ViewBuilder trailing: @escaping () -> Trailing = { EmptyView() }) {
        self.variant = variant
        self.subtitle = subtitle
        self.environmentBadge = environmentBadge
        self.trailing = trailing
    }

    var body: some View {
        HStack(spacing: VLSpacing.sm) {
            logoMark
            VStack(alignment: .leading, spacing: 0) {
                Text(titleText)
                    .font(VLFont.h2)
                    .foregroundColor(titleColor)
                    .tracking(variant == .debugger ? 0.5 : 0)
                if let subtitle {
                    Text(subtitle)
                        .font(VLFont.bodySm)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
            }
            if let badge = environmentBadge {
                VLStatePill(text: badge, tone: variant == .debugger ? .neutral : .primary)
            }
            Spacer()
            trailing()
        }
        .padding(.horizontal, VLSpacing.safeMargin)
        .padding(.vertical, VLSpacing.md)
        .background(VLColor.surface)
    }

    private var titleText: String {
        switch variant {
        case .debugger: return "SDK_DEBUGGER"
        case .brand:    return "VLPlay SDK"
        }
    }

    private var titleColor: Color {
        switch variant {
        case .debugger: return VLColor.onSurfaceVariant
        case .brand:    return VLColor.primary
        }
    }

    @ViewBuilder
    private var logoMark: some View {
        switch variant {
        case .debugger:
            ZStack {
                RoundedRectangle(cornerRadius: VLRadius.sm, style: .continuous)
                    .fill(VLColor.surfaceContainerHigh)
                    .frame(width: 28, height: 28)
                Image(systemName: VLIcon.developerMode)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        case .brand:
            ZStack {
                RoundedRectangle(cornerRadius: VLRadius.sm, style: .continuous)
                    .fill(VLColor.primary)
                    .frame(width: 28, height: 28)
                    .rotationEffect(.degrees(45))
                Text("VL")
                    .font(.system(size: 11, weight: .heavy))
                    .foregroundColor(.white)
            }
            .frame(width: 32, height: 32)
        }
    }
}

extension VLTopAppBar where Trailing == EmptyView {
    init(variant: VLTopBarVariant = .debugger,
         subtitle: String? = nil,
         environmentBadge: String? = nil) {
        self.init(variant: variant,
                  subtitle: subtitle,
                  environmentBadge: environmentBadge,
                  trailing: { EmptyView() })
    }
}
