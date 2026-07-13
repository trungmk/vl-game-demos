package sdk.vlplay.vn.sample.env

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import sdk.vlplay.vn.antiaddiction.AntiAddictionConfig
import sdk.vlplay.vn.antiaddiction.AntiAddictionManager
import sdk.vlplay.vn.antiaddiction.AntiAddictionStatus
import sdk.vlplay.vn.sample.design.components.AntiAddictionClockState
import sdk.vlplay.vn.sample.design.components.VLConfirmCenter
import sdk.vlplay.vn.sample.design.components.VLLoadingCenter
import sdk.vlplay.vn.sample.design.components.VLToastCenter
import sdk.vlplay.vn.sample.models.TokenRefreshLogEntry
import sdk.vlplay.vn.sample.models.UserUiModel
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * Demo app's central observable state. Parity iOS [SDKEnvironment].
 *
 * Bridges Java SDK callbacks (single-listener static setters) into Compose
 * `mutableStateOf` snapshots so Compose recomposes on:
 *   - sign-in / sign-out / session-expire (via VLPlaySDKManager listeners)
 *   - purchase events (via UserPurchaseListener)
 *   - anti-addiction status updates (via AntiAddictionCallback) plus a 1s
 *     local ticker that interpolates `remainingSessionSeconds` between server
 *     polls so the clock UI feels alive
 *
 * Centers ([toast]/[loading]/[confirm]) are owned here so any screen can fire
 * them without prop-drilling. Mount the matching Hosts in your top-level
 * scaffold once.
 *
 * **Single-listener trade-off**: Android SDK uses static setter listeners that
 * the demo fully owns. Hosting integrators that wire their own listeners must
 * not also instantiate `SDKEnvironment` — install one or the other. The
 * iOS-style multi-observer pattern isn't available without changing the SDK.
 */
class SDKEnvironment : ViewModel() {

    // ---- Centers (UI dispatch) ----
    val toast = VLToastCenter()
    val loading = VLLoadingCenter()
    val confirm = VLConfirmCenter()

    // ---- Auth state ----
    var currentUser by mutableStateOf<UserUiModel?>(UserUiModel.fromSdk(VLPlaySDKManager.userModel))
        private set
    var lastEvent by mutableStateOf("—")
        private set

    // ---- Token refresh log (parity iOS demo2 TokenRefreshLogView) ----
    var tokenRefreshLog by mutableStateOf<List<TokenRefreshLogEntry>>(emptyList())
        private set

    // ---- Anti-addiction projection ----
    var antiAddictionClockState by mutableStateOf(AntiAddictionClockState.Empty)
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastStatusSnapshot: AntiAddictionStatus? = null
    private var lastStatusSyncMillis: Long = 0
    private var ticking = false
    private val ticker: Runnable = object : Runnable {
        override fun run() {
            tickAntiAddiction()
            if (ticking) mainHandler.postDelayed(this, 1000L)
        }
    }

    init {
        installListeners()
        startTicking()
        // Hydrate from any cached AA status the SDK already has
        AntiAddictionManager.getLastStatus()?.let { applyStatus(it, isNew = true) }
    }

    override fun onCleared() {
        super.onCleared()
        stopTicking()
        clearListeners()
    }

    // ---- SDK listener install / clear ----

    private fun installListeners() {
        VLPlaySDKManager.setUserSignInListener(object : VLPlaySDKManager.UserSignInListener {
            override fun onSignInSuccess() {
                mainHandler.post {
                    val ui = UserUiModel.fromSdk(VLPlaySDKManager.userModel)
                    currentUser = ui
                    lastEvent = "signed in as ${ui?.displayName ?: "?"}"
                    toast.success(
                        title = "Đăng nhập thành công",
                        message = "Welcome ${ui?.displayName ?: "back"}",
                    )
                }
            }

            override fun onSignInFail() {
                mainHandler.post {
                    lastEvent = "sign in failed"
                    toast.error("Đăng nhập thất bại")
                }
            }
        })

        VLPlaySDKManager.setUserSignOutListener {
            mainHandler.post {
                currentUser = null
                lastEvent = "signed out"
                toast.info("Đã đăng xuất")
                lastStatusSnapshot = null
                lastStatusSyncMillis = 0
                antiAddictionClockState = AntiAddictionClockState.Empty
            }
        }

        VLPlaySDKManager.setUserPurchaseListener(object : VLPlaySDKManager.UserPurchaseListener {
            override fun onPurchaseSuccess() {
                mainHandler.post {
                    lastEvent = "purchase ok"
                    toast.success("Mua thành công")
                }
            }

            override fun onPurchaseFail() {
                mainHandler.post {
                    lastEvent = "purchase failed"
                    toast.error("Mua thất bại")
                }
            }

            override fun onReLogin() {
                mainHandler.post {
                    lastEvent = "purchase requires re-login"
                    toast.warning("Vui lòng đăng nhập lại để hoàn tất giao dịch")
                }
            }
        })

        VLPlaySDKManager.setSessionExpireListener {
            mainHandler.post {
                lastEvent = "session expired"
                toast.error("Session expired", "Vui lòng đăng nhập lại")
            }
        }

        AntiAddictionManager.setCallback(object : AntiAddictionManager.AntiAddictionCallback {
            override fun onConfigLoaded(config: AntiAddictionConfig?) {
                // No state — config is read on demand from AntiAddictionManager.getConfig()
            }

            override fun onStatusReceived(status: AntiAddictionStatus?) {
                if (status == null) return
                mainHandler.post { applyStatus(status, isNew = true) }
            }
        })
    }

    private fun clearListeners() {
        VLPlaySDKManager.setUserSignInListener(null)
        VLPlaySDKManager.setUserSignOutListener(null)
        VLPlaySDKManager.setUserPurchaseListener(null)
        VLPlaySDKManager.setSessionExpireListener(null)
        AntiAddictionManager.setCallback(null)
    }

    // ---- AA ticker ----

    private fun startTicking() {
        if (ticking) return
        ticking = true
        mainHandler.post(ticker)
    }

    private fun stopTicking() {
        ticking = false
        mainHandler.removeCallbacks(ticker)
    }

    private fun tickAntiAddiction() {
        // If SDK published a new status (debug-injected or fresh poll), pull it.
        val sdkStatus = AntiAddictionManager.getLastStatus()
        if (sdkStatus != null && sdkStatus !== lastStatusSnapshot) {
            applyStatus(sdkStatus, isNew = true)
            return
        }
        // Else, interpolate seconds locally between server polls.
        val snapshot = lastStatusSnapshot ?: return
        if (lastStatusSyncMillis == 0L) return
        val elapsedSec = ((System.currentTimeMillis() - lastStatusSyncMillis) / 1000L).toInt().coerceAtLeast(0)
        val baseSeconds = countdownBaseSeconds(snapshot)
        val derivedRemaining = (baseSeconds - elapsedSec).coerceAtLeast(0)
        val current = antiAddictionClockState
        if (derivedRemaining != current.remainingSessionSeconds) {
            antiAddictionClockState = current.copy(remainingSessionSeconds = derivedRemaining)
        }
    }

    private fun applyStatus(status: AntiAddictionStatus, isNew: Boolean) {
        lastStatusSnapshot = status
        if (isNew) lastStatusSyncMillis = System.currentTimeMillis()

        antiAddictionClockState = AntiAddictionClockState(
            hasStatus = true,
            remainingSessionSeconds = countdownBaseSeconds(status),
            currentSessionMinutes = status.currentSessionMinutes,
            remainingTodayMinutes = status.remainingMinutesToday.coerceAtLeast(0),
            ageGroup = status.ageGroup ?: "",
            curfewActive = status.isCurfew,
            shouldWarn = status.shouldWarn(),
            shouldKick = status.shouldKick(),
        )
    }

    /**
     * Pick the countdown source. Parity iOS `countdownBaseSeconds(for:)`
     * (AntiAddictionTimerService.swift:124). Per-session limit only applies
     * to underage/teen ageGroups — adult tier has `adultMaxMinutesPerDay`
     * only. When BE returns `remainingSessionMinutes <= 0` (no session cap)
     * fall back to daily remaining so the ring isn't blank.
     */
    private fun countdownBaseSeconds(status: AntiAddictionStatus): Int {
        if (status.remainingSessionMinutes > 0) {
            return status.remainingSessionMinutes * 60
        }
        return status.remainingMinutesToday.coerceAtLeast(0) * 60
    }

    // ---- Demo helpers ----

    /**
     * Append a token-refresh observability entry. Caller wires this from
     * wherever the SDK exposes a refresh hook (currently absent on Android —
     * Phase 9 TokenRefreshLog screen will surface a "not yet wired" notice).
     */
    fun logTokenRefresh(entry: TokenRefreshLogEntry) {
        tokenRefreshLog = listOf(entry) + tokenRefreshLog
    }

    /** Refresh the [currentUser] snapshot from [VLPlaySDKManager.userModel]. */
    fun refreshUserSnapshot() {
        currentUser = UserUiModel.fromSdk(VLPlaySDKManager.userModel)
    }

}

/**
 * Provide an [SDKEnvironment] scoped to the current Compose tree. For the
 * demo app's single Activity model, hold it via `viewModel()` in the root
 * Composable instead of constructing per-screen.
 */
@Composable
fun rememberSDKEnvironmentLocally(): SDKEnvironment = remember { SDKEnvironment() }
