import SwiftUI
import VLPlaySDK

@main
struct VLPlayDemo2App: App {
    @UIApplicationDelegateAdaptor(AppDelegateAdapter.self) var appDelegate
    @StateObject private var env = SDKEnvironment()

    var body: some Scene {
        WindowGroup {
            RootContainerView()
                .environmentObject(env)
                .environmentObject(env.toast)
                .environmentObject(env.loading)
                .environmentObject(env.confirm)
                .onOpenURL { url in
                    // SwiftUI scene-based apps with @UIApplicationDelegateAdaptor do NOT route
                    // incoming URLs through `AppDelegate.application(_:open:options:)`. The Adapter's
                    // application(_:open:options:) is dead code in this lifecycle — URLs go through
                    // SceneDelegate or .onOpenURL only. Without this hook, FB SDK callbacks like
                    // `fb888626750859153://authorize?access_token=...` are dropped, ASWebAuthenticationSession
                    // times out, and FB Login returns `result.isCancelled=YES` even after the user taps Continue.
                    // Same issue would have affected Google Sign-In callback recovery.
                    _ = VLPlaySDKManager.handle(UIApplication.shared, open: url, options: [:])
                }
        }
    }
}
