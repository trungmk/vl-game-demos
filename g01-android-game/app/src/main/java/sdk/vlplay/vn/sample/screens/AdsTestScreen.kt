package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.json.JSONObject
import sdk.vlplay.vn.ads.AdManager
import sdk.vlplay.vn.ads.AdPlacementConfig
import sdk.vlplay.vn.ads.AdsConfig
import sdk.vlplay.vn.ads.VLPlayAds
import sdk.vlplay.vn.ads.listener.AdLoadListener
import sdk.vlplay.vn.ads.listener.InterstitialAdListener
import sdk.vlplay.vn.ads.listener.RewardedAdListener
import sdk.vlplay.vn.ads.model.AdError
import sdk.vlplay.vn.ads.model.AdReward
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.tracking.VLPlaySDKManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local AdMob TEST config (Phase B) — Google's PUBLIC test ad units fill
 * everywhere (even emulator) with NO account. `ssv:false` → the reward stays
 * advisory (no BE ticket needed); the reward-confirm leg needs BE-14 + a real
 * AdMob unit with an SSV URL. Injecting this via [AdManager] overrides the CMS
 * ads config locally so the pipeline is device-testable before provisioning.
 */
private const val ADMOB_TEST_CONFIG_JSON =
    "{" +
        "\"enabled\":true," +
        "\"provider\":\"admob\"," +
        "\"testMode\":true," +
        "\"placements\":[" +
            "{\"id\":\"rewarded_test\",\"format\":\"rewarded\"," +
                "\"unitId\":\"ca-app-pub-3940256099942544/5224354917\"," +
                "\"preload\":true,\"ssv\":false,\"rewardCurrency\":\"gold\",\"rewardAmount\":10}," +
            "{\"id\":\"interstitial_test\",\"format\":\"interstitial\"," +
                "\"unitId\":\"ca-app-pub-3940256099942544/1033173712\",\"preload\":true}" +
        "]" +
    "}"

/**
 * IAA test console (P2-7) — exercises the provider-agnostic [VLPlayAds]
 * facade: per-placement Preload / isReady / Show + a live event log.
 *
 * INTEGRITY demo rule: the "reward" toast fires ONLY on [RewardedAdListener
 * .onRewardConfirmed] (BE SSV) — [onUserRewarded] is logged as advisory,
 * exactly what partner games must do.
 *
 * Needs the remote `ads` block enabled for the current env (stg test game
 * `69ec2ed7…` is provisioned; prod is OFF until go-live) + the AppLovin
 * dependency in this app (present) + a real sdkKey/unitId from the MAX
 * account before fills actually load.
 */
@Composable
fun AdsTestScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val activity = LocalContext.current as? Activity
    var tick by remember { mutableIntStateOf(0) }
    val log = remember { mutableStateListOf<String>() }

    fun addLog(line: String) {
        log.add(0, "${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}  $line")
        while (log.size > 80) log.removeAt(log.size - 1)
    }

    // 1s ticker so isReady badges + enabled/init status stay live without
    // wiring extra callbacks.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            tick++
        }
    }

    // Live applied config (reflects CMS OR the local AdMob test inject); fall
    // back to the CMS cache before the ads module has applied anything.
    val ads = remember(tick) { AdManager.getConfig() ?: VLPlaySDKManager.getCurrentConfig()?.ads }
    val enabled = remember(tick) { VLPlayAds.isEnabled() }
    val initialized = remember(tick) { VLPlayAds.isInitialized() }

    val loadListener = remember {
        object : AdLoadListener {
            override fun onAdLoaded(placementId: String) = addLog("✔ loaded [$placementId]")
            override fun onAdLoadFailed(placementId: String, error: AdError) =
                addLog("✖ load failed [$placementId] ${error.code}: ${error.message}")
        }
    }

    val rewardedListener = remember {
        object : RewardedAdListener {
            override fun onUserRewarded(placementId: String, reward: AdReward) =
                addLog("△ ADVISORY reward [$placementId] $reward — chờ SSV, KHÔNG grant")

            override fun onRewardConfirmed(placementId: String, serverReward: AdReward) {
                addLog("★ CONFIRMED [$placementId] +${serverReward.amount} ${serverReward.currency} (nonce ${serverReward.nonce.take(8)}…)")
                env.toast.success(
                    "Reward confirmed (SSV)",
                    "+${serverReward.amount} ${serverReward.currency} — grant tại đây",
                )
            }

            override fun onAdDismissed(placementId: String) = addLog("· dismissed [$placementId]")
            override fun onAdShowFailed(placementId: String, error: AdError) =
                addLog("✖ show failed [$placementId] ${error.code}: ${error.message}")

            override fun onAdShown(placementId: String) = addLog("▶ shown [$placementId]")
            override fun onAdClicked(placementId: String) = addLog("☞ clicked [$placementId]")
        }
    }

    val interstitialListener = remember {
        object : InterstitialAdListener {
            override fun onAdDismissed(placementId: String) = addLog("· dismissed [$placementId]")
            override fun onAdShowFailed(placementId: String, error: AdError) =
                addLog("✖ show failed [$placementId] ${error.code}: ${error.message}")

            override fun onAdShown(placementId: String) = addLog("▶ shown [$placementId]")
            override fun onAdClicked(placementId: String) = addLog("☞ clicked [$placementId]")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
    ) {
        VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Ads Test (IAA)")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            StatusCard(
                enabled = enabled,
                initialized = initialized,
                provider = ads?.provider ?: "—",
                testMode = ads?.isTestMode == true,
            )

            LocalTestConfigCard(
                onLoadAdmob = load@{
                    val app: Application = activity?.application ?: run {
                        addLog("✖ no Application context")
                        return@load
                    }
                    val cfg = AdsConfig.fromJson(JSONObject(ADMOB_TEST_CONFIG_JSON))
                    AdManager.init(app, cfg)       // cold path: first enable
                    AdManager.applyConfig(cfg)     // warm path: swap if already inited
                    addLog("⚙ nạp AdMob TEST config — provider=${cfg.provider}, ${cfg.placements.size} placement (test units)")
                    tick++
                },
                onPreloadAll = {
                    addLog("→ preloadAll")
                    VLPlayAds.preloadAll()
                    tick++
                },
            )

            val placements = ads?.placements.orEmpty()
            if (placements.isEmpty()) {
                EmptyHint(enabled = enabled)
            }
            placements.forEach { placement ->
                PlacementCard(
                    placement = placement,
                    ready = remember(tick) { VLPlayAds.isReady(placement.id) },
                    onPreload = {
                        addLog("→ preload [${placement.id}]")
                        VLPlayAds.preload(placement.id, loadListener)
                    },
                    onShow = show@{
                        val act = activity ?: return@show
                        addLog("→ show [${placement.id}]")
                        when (placement.format) {
                            AdPlacementConfig.FORMAT_REWARDED ->
                                VLPlayAds.showRewarded(act, placement.id, rewardedListener)
                            AdPlacementConfig.FORMAT_INTERSTITIAL ->
                                VLPlayAds.showInterstitial(act, placement.id, interstitialListener)
                            else -> addLog("✖ format [${placement.format}] chưa hỗ trợ (P3)")
                        }
                    },
                )
            }

            LogCard(lines = log)
        }
    }
}

@Composable
private fun StatusCard(enabled: Boolean, initialized: Boolean, provider: String, testMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
        ) {
            Text("Module status", style = VLFont.labelBold, color = VLColor.OnSurfaceVariant)
            StatusRow("ads.enabled (config)", if (enabled) "ON" else "OFF", ok = enabled)
            StatusRow("provider initialized", if (initialized) "YES" else "NO", ok = initialized)
            StatusRow("provider", provider, ok = true)
            StatusRow("testMode", if (testMode) "ON" else "OFF", ok = testMode)
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, ok: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = VLFont.bodySm, color = VLColor.OnSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            value,
            style = VLFont.bodySm.copy(fontWeight = FontWeight.SemiBold),
            color = if (ok) VLColor.Primary else VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun LocalTestConfigCard(onLoadAdmob: () -> Unit, onPreloadAll: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Text("Local test config", style = VLFont.labelBold, color = VLColor.OnSurfaceVariant)
            Text(
                "AdMob test units fill mọi nơi (cả emulator), KHÔNG cần account. " +
                    "Nạp → override CMS config local → Preload → Show. Reward là advisory " +
                    "(ssv:false) — confirm cần BE-14 + real unit.",
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
                Box(modifier = Modifier.weight(1f)) {
                    VLButton(text = "Nạp AdMob TEST", onClick = onLoadAdmob)
                }
                Box(modifier = Modifier.weight(1f)) {
                    VLButton(text = "Preload all", onClick = onPreloadAll, kind = VLButtonKind.Secondary)
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(enabled: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Text(
            text = if (enabled) {
                "Config bật ads nhưng không có placement nào."
            } else {
                "Ads đang TẮT trên env hiện tại (prod OFF tới go-live). " +
                    "Test ads → trỏ demo về STG: game test đã provision placement rewarded_test."
            },
            style = VLFont.bodySm,
            color = VLColor.OnSurfaceVariant,
            modifier = Modifier.padding(VLSpacing.md),
        )
    }
}

@Composable
private fun PlacementCard(
    placement: AdPlacementConfig,
    ready: Boolean,
    onPreload: () -> Unit,
    onShow: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        placement.id,
                        style = VLFont.bodyLg.copy(fontWeight = FontWeight.SemiBold),
                        color = VLColor.OnSurface,
                    )
                    Text(
                        buildString {
                            append(placement.format)
                            if (placement.isSsv) append(" · SSV")
                            if (placement.unitId.isNullOrEmpty()) append(" · ⚠ unitId trống")
                        },
                        style = VLFont.bodySm,
                        color = VLColor.OnSurfaceVariant,
                    )
                }
                ReadyBadge(ready = ready)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
                Box(modifier = Modifier.weight(1f)) {
                    VLButton(text = "Preload", onClick = onPreload, kind = VLButtonKind.Secondary)
                }
                Box(modifier = Modifier.weight(1f)) {
                    VLButton(text = "Show", onClick = onShow)
                }
            }
        }
    }
}

@Composable
private fun ReadyBadge(ready: Boolean) {
    Surface(
        shape = RoundedCornerShape(VLRadius.sm),
        color = if (ready) VLColor.PrimaryContainer else VLColor.SurfaceContainerHigh,
    ) {
        Text(
            text = if (ready) "READY" else "NOT READY",
            style = VLFont.labelBold,
            color = if (ready) VLColor.OnPrimaryContainer else VLColor.OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = VLSpacing.sm, vertical = 4.dp),
        )
    }
}

@Composable
private fun LogCard(lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("Event log", style = VLFont.labelBold, color = VLColor.OnSurfaceVariant)
            if (lines.isEmpty()) {
                Text(
                    "Chưa có event — Preload/Show để bắt đầu.",
                    style = VLFont.bodySm,
                    color = VLColor.OnSurfaceVariant,
                )
            }
            lines.forEach { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = VLColor.OnSurface,
                )
            }
        }
    }
}
