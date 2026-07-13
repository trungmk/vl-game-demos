package sdk.vlplay.vn.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.Color as AndroidColor
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.nav.AppNavGraph
import sdk.vlplay.vn.tracking.VLPlaySDKManager

class ComposeMainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result not used — OneSignal (D6.5) observes the grant result itself */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: hero background draws under status bar + gesture pill.
        // setStatusBarColor(TRANSPARENT) alone leaves a contrast scrim on API 29+ —
        // disabling enforcement + opting into the display cutout area is what
        // actually pushes the hero all the way to the device edges.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // Force verbose SDK logs even on Play Console release builds — needed
        // for diagnosing Google login / IAP on Internal-testing AABs (Play
        // re-signs with App Signing key, so device build ≠ Studio debug).
        // STRIP before production publish: request bodies + tokens log clear-text.
        VLPlaySDKManager.setVerboseLogging(true)
        // Activity-scoped SDK init: reads sdk_client_id/secret + api key from
        // sdkconfig.xml, sets x-api-key on the http client, loads cached SDKConfig,
        // and fetches /sdk/config so VLPlayString.GOOGLE_CLIENT_ID etc. populate
        // before LoginActivity's social tap guards run.
        VLPlaySDKManager.initStartSDK(this)
        // Warm up the login UI while the game is still loading so the first
        // signIn() popup renders instantly (pays one-time class-load + inflate +
        // drawable-decode costs off the main thread). Idempotent + non-blocking;
        // must run after initStartSDK so apiKey/config are loaded.
        VLPlaySDKManager.prepareSignIn(this)
        // FAB Phase 1 seed — parity iOS demo2 AppDelegateAdapter. Three calls
        // total are all the partner game needs:
        //   1. configureFAB — seeds defaults BEFORE CMS lands (dev only).
        //      When BE ships the `fab` block this becomes redundant (spec §8.2
        //      local < CMS).
        //   2. setFABContext — fed every time the game knows current
        //      server + character (Support tab guard requires both ≥ 6 chars).
        //   3. (Optional) showHud — open HUD programmatically from any button.
        // Auto-fallback to standalone account / support popup when CMS has
        // fab.enabled = false. The ball auto-appears on every sign-in and
        // auto-disappears on signOut / sessionExpired.
        VLPlaySDKManager.configureFAB(
            application,
            mapOf(
                "enabled" to true,
                "tabs" to listOf("account", "support", "identity"),
                "position" to "right"
            )
        )
        requestNotificationPermissionIfNeeded()
        setContent {
            VLTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.bg_app_hero),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        AppNavGraph()
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
