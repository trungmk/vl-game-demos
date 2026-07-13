import SwiftUI
import VLPlaySDK

// Switches between MainTabView (signed in) and auto-launched SDK sign-in
// popup (signed out). Hosts global overlays: snackbar, loading, confirm modal.

struct RootContainerView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var loading: VLLoadingCenter
    @EnvironmentObject var confirm: VLConfirmCenter

    var body: some View {
        ZStack {
            VLColor.surface.ignoresSafeArea()
            Group {
                if env.currentUser != nil {
                    MainTabView()
                } else {
                    // Empty surface; SDK sign-in popup fires on appear. Re-appears
                    // on sign-out because the Group re-mounts this branch.
                    Color.clear
                        .onAppear { VLPlaySDKManager.default().signIn() }
                }
            }

            if let pending = confirm.pending {
                VLConfirmModal(
                    content: pending,
                    onConfirm: { confirm.confirm() },
                    onCancel:  { confirm.cancel()  }
                )
            }

            VLLoadingOverlay(center: loading)
        }
        .overlay(alignment: .bottom) {
            VLToastOverlay(center: toast)
        }
        .preferredColorScheme(.light)
    }
}
