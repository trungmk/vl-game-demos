import UIKit
import VLPlaySDK

final class AppDelegateAdapter: NSObject, UIApplicationDelegate {
    // Some legacy libraries embedded in VLPlaySDK reach into
    // `UIApplication.shared.delegate.window` during setup. SwiftUI's scene-based adapter
    // does not provide one by default, so expose the current key window here.
    @objc var window: UIWindow?

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        self.window = application.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first
        // Configure env BEFORE -handle: which calls -initSDK (debug guard) and BEFORE
        // NetworkModal +sharedModal captures the baseURL via dispatch_once.
        let sdk = VLPlaySDKManager.default()
        sdk.isSandbox = false   // production gateway → https://gw.vlplay.vn
        sdk.ignoreCaptcha = false
        sdk.isSaveAccessToken = true
        // Diagnostics: full AppsFlyer network logs in the console. Logging only
        // — event names/data stay production. Strip before App Store builds.
        VLPlaySDKManager.setVerboseLogging(true)
        VLPlaySDKManager.handle(application, didFinishLaunchingWithOptions: launchOptions ?? [:])

        // Demo2 design language is dark — match the SDK's progress HUD so the
        // IAP verify banner and login spinner blend with the rest of the app.
        VLPlaySDKManager.setHUDStyle(.dark)

        // NATIVE-FAB Phase 2 — partner-game integration is exactly three calls
        // (rest of the FAB UI lives inside the SDK).
        //
        //   1. configureFAB — seeds defaults BEFORE CMS lands (dev only). When
        //      BE ships the `fab` block, this becomes redundant and CMS wins
        //      per spec §8.2 (local < CMS).
        //   2. setContext — fed every time the game knows the current server +
        //      character. Support tab's BE ticket submit requires both ≥ 6
        //      chars (FAB-HUD-SPEC §4.2 guard).
        //   3. (Optional) showHud — game can open the HUD programmatically
        //      from any button. Auto-fallback to standalone account / support
        //      popup when CMS has `fab.enabled = false`.
        VLPlaySDKManager.configureFAB([
            "enabled": true,
            "tabs": ["account", "support", "identity"],
            "position": "right",
        ])

        return true
    }

    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        let stringOptions = Dictionary(uniqueKeysWithValues: options.map { ($0.key.rawValue, $0.value) })
        return VLPlaySDKManager.handle(app, open: url, options: stringOptions)
    }
}
