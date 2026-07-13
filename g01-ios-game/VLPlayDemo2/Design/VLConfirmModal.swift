import SwiftUI

// Centered confirm modal — used for logout, delete account, destructive actions.
// Variant `.destructive` shows red triangle warning icon + red primary button.

enum VLConfirmModalVariant {
    case info, destructive
}

struct VLConfirmModalContent: Identifiable, Equatable {
    let id = UUID()
    let title: String
    let message: String
    let confirmTitle: String
    let cancelTitle: String
    let variant: VLConfirmModalVariant
}

struct VLConfirmModal: View {
    let content: VLConfirmModalContent
    var onConfirm: () -> Void
    var onCancel: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.45)
                .ignoresSafeArea()
                .onTapGesture { onCancel() }

            VStack(spacing: VLSpacing.md) {
                if content.variant == .destructive {
                    Image(systemName: VLIcon.warning)
                        .font(.system(size: 36, weight: .semibold))
                        .foregroundColor(VLColor.error)
                        .padding(.top, VLSpacing.sm)
                }
                Text(content.title)
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.onSurface)
                Text(content.message)
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .multilineTextAlignment(.center)

                HStack(spacing: VLSpacing.sm) {
                    Button(content.cancelTitle, action: onCancel)
                        .vlButton(.secondary)
                    Button(content.confirmTitle, action: onConfirm)
                        .vlButton(content.variant == .destructive ? .destructive : .primary)
                }
                .padding(.top, VLSpacing.sm)
            }
            .padding(VLSpacing.lg)
            .frame(maxWidth: 320)
            .background(VLColor.surfaceContainerLowest)
            .clipShape(RoundedRectangle(cornerRadius: VLRadius.lg, style: .continuous))
            .shadow(color: VLElevation.ambient,
                    radius: VLElevation.ambientRadius,
                    y: VLElevation.ambientYOffset)
            .padding(VLSpacing.lg)
        }
        .transition(.opacity.combined(with: .scale(scale: 0.95)))
        .zIndex(998)
    }
}

final class VLConfirmCenter: ObservableObject {
    @Published var pending: VLConfirmModalContent? = nil
    private var onConfirm: (() -> Void)?
    private var onCancel: (() -> Void)?

    func ask(_ content: VLConfirmModalContent,
             onConfirm: @escaping () -> Void,
             onCancel: @escaping () -> Void = {}) {
        DispatchQueue.main.async {
            self.pending = content
            self.onConfirm = onConfirm
            self.onCancel = onCancel
        }
    }

    func confirm() {
        let cb = onConfirm
        clear()
        cb?()
    }

    func cancel() {
        let cb = onCancel
        clear()
        cb?()
    }

    private func clear() {
        pending = nil
        onConfirm = nil
        onCancel = nil
    }
}
