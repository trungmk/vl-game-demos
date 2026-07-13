import SwiftUI

enum VLToastKind { case info, success, error, warning }

struct VLToastConfig: Equatable {
    let kind: VLToastKind
    let title: String
    var message: String? = nil
}

final class VLToastCenter: ObservableObject {
    @Published var toast: VLToastConfig?
    private var dismissTask: DispatchWorkItem?

    func show(_ config: VLToastConfig, duration: TimeInterval = 2.4) {
        DispatchQueue.main.async {
            self.toast = config
            self.dismissTask?.cancel()
            let task = DispatchWorkItem { [weak self] in self?.toast = nil }
            self.dismissTask = task
            DispatchQueue.main.asyncAfter(deadline: .now() + duration, execute: task)
        }
    }

    func success(_ title: String, _ message: String? = nil) { show(.init(kind: .success, title: title, message: message)) }
    func error(_ title: String, _ message: String? = nil)   { show(.init(kind: .error,   title: title, message: message)) }
    func info(_ title: String, _ message: String? = nil)    { show(.init(kind: .info,    title: title, message: message)) }
    func warning(_ title: String, _ message: String? = nil) { show(.init(kind: .warning, title: title, message: message)) }
}

struct VLToastOverlay: View {
    @ObservedObject var center: VLToastCenter
    var body: some View {
        ZStack(alignment: .bottom) {
            if let t = center.toast {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: icon(for: t.kind))
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(tint(for: t.kind))
                    VStack(alignment: .leading, spacing: 2) {
                        Text(t.title).font(.system(size: 14, weight: .semibold))
                        if let m = t.message { Text(m).font(VLFont.caption).foregroundColor(VLColor.mutedText).lineLimit(3) }
                    }
                    Spacer()
                }
                .padding(14)
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(VLColor.border, lineWidth: 0.5)
                )
                .shadow(color: Color.black.opacity(0.15), radius: 12, y: 6)
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.85), value: center.toast)
    }

    private func icon(for kind: VLToastKind) -> String {
        switch kind {
        case .info:    return "info.circle.fill"
        case .success: return "checkmark.circle.fill"
        case .error:   return "xmark.octagon.fill"
        case .warning: return "exclamationmark.triangle.fill"
        }
    }

    private func tint(for kind: VLToastKind) -> Color {
        switch kind {
        case .info:    return VLColor.accent
        case .success: return VLColor.success
        case .error:   return VLColor.danger
        case .warning: return VLColor.warning
        }
    }
}

struct VLToastKey: EnvironmentKey { static let defaultValue = VLToastCenter() }
extension EnvironmentValues {
    var vlToast: VLToastCenter {
        get { self[VLToastKey.self] }
        set { self[VLToastKey.self] = newValue }
    }
}
