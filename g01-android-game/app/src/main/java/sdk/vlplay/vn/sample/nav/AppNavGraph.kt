package sdk.vlplay.vn.sample.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.sample.screens.AdsTestScreen
import sdk.vlplay.vn.sample.screens.AntiAddictionDebugScreen
import sdk.vlplay.vn.sample.screens.AppsFlyerEventsScreen
import sdk.vlplay.vn.sample.screens.BindSocialScreen
import sdk.vlplay.vn.sample.screens.DeleteAccountScreen
import sdk.vlplay.vn.sample.screens.GiftcodeRedeemScreen
import sdk.vlplay.vn.sample.screens.IdentityVerificationScreen
import sdk.vlplay.vn.sample.screens.MainTabScreen
import sdk.vlplay.vn.sample.screens.ProfileScreen
import sdk.vlplay.vn.sample.screens.PurchaseHistoryScreen
import sdk.vlplay.vn.sample.screens.RemoteConfigScreen
import sdk.vlplay.vn.sample.screens.ShopScreen
import sdk.vlplay.vn.sample.screens.TokenRefreshLogScreen
import sdk.vlplay.vn.sample.screens.UpgradeGuestScreen
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * App navigation graph. Phases shipped:
 *
 *   Phase 5  Auto-launch native LoginActivity → MainTab (no pre-auth UI)
 *   Phase 6  MainTab (3 tabs) + Profile
 *   Phase 7  BindSocial + UpgradeGuest + IdentityVerification + DeleteAccount
 *   Phase 8  Shop + PurchaseHistory
 *   Phase 9  GiftcodeRedeem + Debug{AA,TokenLog,RemoteConfig}
 *  Phase 10A UI/UX align vs iOS demo2; reactive auth-state nav (parity iOS
 *           [RootContainerView] which conditions MainTabView vs auto sign-in on
 *           `env.currentUser != nil`).
 *
 * NATIVE-AUTH-MIGRATION (parity iOS Phase 1): Web SDK route ripped
 * 2026-05-08 LATE+1. Sign-in launches SDK's native [LoginActivity] via
 * `VLPlaySDKManager.signIn(activity)`. Auth completion is observed via
 * [SDKEnvironment.currentUser] which the env populates from the
 * `UserSignInListener` callback. The post-auth navigation is driven from
 * a single top-level [LaunchedEffect] keyed on `env.currentUser` — so
 * **any** sign-in path (social / email / guest / cached re-hydrate) lands
 * on [Routes.MainTab] and **any** sign-out (logout button / deactivate
 * account / session expire) collapses back to [Routes.Home]. Mirrors iOS
 * [RootContainerView] state-driven swap exactly.
 *
 * [SDKEnvironment] is owned at this scope via `viewModel()` so a single
 * instance backs every screen — listener install/clear happens once and
 * the same `currentUser` / `antiAddictionClockState` propagate through
 * the tab graph.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.Home,
) {
    val env: SDKEnvironment = viewModel()

    // Reactive auth-state navigation (parity iOS RootContainerView pattern):
    // currentUser flips → drive nav from anywhere in the graph. Avoids the
    // failure mode where a per-screen `LaunchedEffect` only triggered while
    // Home was the active composable, missing cached re-hydration races and
    // any future deep-link entries.
    LaunchedEffect(env.currentUser) {
        val user = env.currentUser
        val currentRoute = navController.currentDestination?.route
        if (user != null) {
            // Just authenticated (any path). Promote pre-auth Home → MainTab,
            // clearing Home from the back-stack so back-press from MainTab
            // exits the app instead of bouncing to Home.
            if (currentRoute == null || currentRoute == Routes.Home) {
                env.refreshUserSnapshot()
                navController.navigate(Routes.MainTab) {
                    popUpTo(Routes.Home) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else {
            // Just signed out / deactivated / session expired. If we're
            // anywhere above Home, collapse the stack back to pre-auth.
            if (currentRoute != null && currentRoute != Routes.Home) {
                navController.navigate(Routes.Home) {
                    popUpTo(Routes.Home) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Home) {
            // Pre-auth holder: render an empty surface and fire the SDK
            // sign-in popup immediately. Re-enters this composable on every
            // sign-out (top-level reactive LaunchedEffect routes back here),
            // so the popup re-appears whenever the user is unauthenticated.
            val context = LocalContext.current
            val activity = remember(context) { context.findActivity() }
            LaunchedEffect(Unit) {
                activity?.let { VLPlaySDKManager.signIn(it) }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VLColor.Surface),
            )
        }

        composable(Routes.MainTab) {
            val mainTabContext = LocalContext.current
            val mainTabActivity = remember(mainTabContext) { mainTabContext.findActivity() }
            MainTabScreen(
                env = env,
                onProfile = { navController.navigate(Routes.Profile) },
                onBind = { navController.navigate(Routes.BindSocial) },
                // Demonstrates the public SDK entry point a game can call from its own
                // button: presents the SDK's identity-verification popup directly.
                onIdentity = { mainTabActivity?.let { VLPlaySDKManager.showIdentityVerification(it) } },
                onDeleteAccount = { navController.navigate(Routes.DeleteAccount) },
                onGiftcode = { navController.navigate(Routes.GiftcodeRedeem) },
                onDebugAntiAddiction = { navController.navigate(Routes.DebugAntiAddiction) },
                onDebugTokenLog = { navController.navigate(Routes.DebugTokenLog) },
                onDebugRemoteConfig = { navController.navigate(Routes.DebugRemoteConfig) },
                onAFEvents = { navController.navigate(Routes.AppsFlyerEvents) },
                onAds = { navController.navigate(Routes.DebugAds) },
                onShop = { navController.navigate(Routes.Shop) },
                // Sign-out nav is driven by the top-level reactive
                // LaunchedEffect on env.currentUser. SDK signOut →
                // UserSignOutListener → env.currentUser = null → global
                // LaunchedEffect collapses stack back to Home.
                onSignedOut = {},
            )
        }

        composable(Routes.Profile) {
            ProfileScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BindSocial) {
            BindSocialScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.UpgradeGuest) {
            UpgradeGuestScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.IdentityVerification) {
            IdentityVerificationScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DeleteAccount) {
            DeleteAccountScreen(
                env = env,
                onCancel = { navController.popBackStack() },
                // Post-deactivate nav is driven by the top-level reactive
                // LaunchedEffect on env.currentUser. DeleteAccountScreen's
                // listener calls env.refreshUserSnapshot() → snapshot becomes
                // null (SDK cleared userModel during deactivate) → global
                // LaunchedEffect collapses stack back to Home.
                onDeactivated = {},
            )
        }

        composable(Routes.Shop) {
            ShopScreen(
                env = env,
                onBack = { navController.popBackStack() },
                onHistory = { navController.navigate(Routes.PurchaseHistory) },
            )
        }

        composable(Routes.PurchaseHistory) {
            PurchaseHistoryScreen(
                env = env,
                onBack = { navController.popBackStack() },
                mountHosts = true,
            )
        }

        composable(Routes.GiftcodeRedeem) {
            GiftcodeRedeemScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DebugAntiAddiction) {
            AntiAddictionDebugScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DebugTokenLog) {
            TokenRefreshLogScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DebugRemoteConfig) {
            RemoteConfigScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.AppsFlyerEvents) {
            AppsFlyerEventsScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DebugAds) {
            AdsTestScreen(
                env = env,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
