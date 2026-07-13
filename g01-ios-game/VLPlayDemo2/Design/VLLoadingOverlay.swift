import SwiftUI

// Non-modal HUD-style centered spinner — host-owned, parity the SDK's
// VLPlayLoading dark card (the SDK side is now SVProgressHUD-free).
//
// Two bugs being fixed:
//   * iOS 26: a ZStack without an explicit frame now sizes to its largest
//     intrinsic child (Color.opacity no longer dictates size), so a
//     full-screen dim shrank to a tiny square in the top-left corner.
//   * iOS 18: the full-screen `Color.black.opacity(0.35).ignoresSafeArea()`
//     dim caught every hit, so when an SDK-presented popup (login,
//     IAP HUD, identity OTP, …) was visible behind it the popup buttons
//     became un-tappable.
//
// Fix: drop the full-screen dim. Show a dark HUD card centered in the
// available space with an explicit `.frame(maxWidth: .infinity,
// maxHeight: .infinity)` outer so the centering math doesn't depend on
// implicit ZStack sizing. The outer container does not catch hits
// (`allowsHitTesting(false)`) so SDK popups stay interactive — the small
// spinner card itself is the only visible feedback that work is in
// progress.

struct VLLoadingOverlay: View {
    @ObservedObject var center: VLLoadingCenter

    var body: some View {
        if center.isLoading {
            VStack(spacing: VLSpacing.md) {
                ProgressView()
                    .progressViewStyle(.circular)
                    .tint(.white)
                    .scaleEffect(1.4)
                if let message = center.message {
                    Text(message)
                        .font(VLFont.bodyMd)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                }
            }
            .padding(.horizontal, VLSpacing.lg)
            .padding(.vertical, VLSpacing.lg)
            .frame(minWidth: 120, minHeight: 120)
            .background(
                RoundedRectangle(cornerRadius: VLRadius.lg, style: .continuous)
                    .fill(Color.black.opacity(0.80))
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .allowsHitTesting(false)
            .transition(.opacity)
            .zIndex(999)
        }
    }
}

final class VLLoadingCenter: ObservableObject {
    @Published var isLoading: Bool = false
    @Published var message: String? = nil

    func show(_ message: String? = nil) {
        DispatchQueue.main.async {
            self.message = message
            self.isLoading = true
        }
    }

    func hide() {
        DispatchQueue.main.async {
            self.isLoading = false
            self.message = nil
        }
    }
}
