package sdk.vlplay.vn.sample.nav

/**
 * Navigation route constants — parity iOS demo2 [RootContainerView] +
 * [MainTabView] navigation tree. Phases 5-9 add screens incrementally.
 *
 * NATIVE-AUTH-MIGRATION (parity iOS Phase 1): Web SDK login route ripped
 * 2026-05-08 LATE+1. Sign-in launches SDK's native [LoginActivity] directly
 * via `VLPlaySDKManager.signIn(activity)`; auth completion is observed via
 * `SDKEnvironment.currentUser` (UserSignInListener wired in env init).
 */
object Routes {
    // Pre-auth flow
    const val Home = "home"

    // Post-auth main tab graph
    const val MainTab = "main_tab"

    // 4 bottom-nav root tabs (children of MainTab)
    const val TabHub = "tab/hub"
    const val TabShop = "tab/shop"
    const val TabRewards = "tab/rewards"
    const val TabProfile = "tab/profile"

    // Auth ops (presented from Profile or Hub)
    const val Profile = "profile"
    const val BindSocial = "bind_social"
    const val UpgradeGuest = "upgrade_guest"
    const val IdentityVerification = "identity_verification"
    const val DeleteAccount = "delete_account"

    // Logs
    const val Shop = "shop"
    const val PurchaseHistory = "purchase_history"

    // Rewards
    const val GiftcodeRedeem = "giftcode_redeem"

    // Tools
    const val AppsFlyerEvents = "af_events"
    const val DebugAds = "debug/ads"

    // Debug
    const val DebugRoot = "debug/root"
    const val DebugAntiAddiction = "debug/anti_addiction"
    const val DebugRemoteConfig = "debug/remote_config"
    const val DebugTokenLog = "debug/token_log"
}
