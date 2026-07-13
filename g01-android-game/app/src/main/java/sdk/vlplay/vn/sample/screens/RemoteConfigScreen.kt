package sdk.vlplay.vn.sample.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONObject
import sdk.vlplay.vn.antiaddiction.AntiAddictionConfig
import sdk.vlplay.vn.antiaddiction.AntiAddictionManager
import sdk.vlplay.vn.common.VLPlayString
import sdk.vlplay.vn.config.SdkConfig
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLPillTone
import sdk.vlplay.vn.sample.design.components.VLStatePill
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * Comprehensive read-only inspector for the CMS-driven SDK config —
 * `GET /api/v1/sdk/config?clientId=...`. Parity iOS demo2 [RemoteConfigView]
 * but extended for "toàn game" coverage: surfaces every public setter the
 * SDK exposes (plus AppsFlyer IDs + AntiAddiction limits) because
 * integrators routinely need to verify each toggle reaches the device —
 * see memory `sdk_config_spec_drift` for the CMS write divergence bug
 * we keep tripping on.
 *
 * Sections:
 * 1. **Status** — loaded flag, ClientID (host plist), SDK min version
 * 2. **Features** — 6 CMS toggles via `isFeatureEnabled` (incl. emailVerification Day 24)
 * 3. **Payment Methods** — 4 method ids via `isPaymentMethodEnabled`
 * 4. **Support** — hotline / email / fanpage with tap-to-open intents
 * 5. **Tracking IDs** — AppsFlyer dev key + app id
 * 6. **Anti-Addiction** — config thresholds (per-age limits + curfew + polling)
 * 7. **Raw snapshot** — pretty-printed JSON of `SdkConfig.toJson()` (debug)
 */
@Composable
fun RemoteConfigScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    var refreshTick by remember { mutableStateOf(0) }
    val config = remember(refreshTick) { VLPlaySDKManager.getCurrentConfig() }
    val ready = remember(refreshTick) { VLPlaySDKManager.isConfigReady() }
    val aaConfig = remember(refreshTick) { AntiAddictionManager.getConfig() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Remote Config")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Header(
                    onRefresh = {
                        refreshTick++
                        env.toast.info("Đã refresh", "Đọc lại snapshot từ SdkConfigManager")
                    },
                )

                StatusSection(ready = ready, config = config)

                FeaturesSection()

                PaymentMethodsSection()

                SupportSection(config = config, context = context, env = env)

                TrackingIdsSection(config = config)

                AntiAddictionSection(aaConfig = aaConfig)

                RawSnapshotSection(config = config)

                Spacer(modifier = Modifier.height(VLSpacing.md))
                VLButton(
                    text = "← Quay lại",
                    onClick = onBack,
                    kind = VLButtonKind.Ghost,
                )
            }
        }

        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

@Composable
private fun Header(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Remote SDK Config",
                style = VLFont.h1,
                color = VLColor.OnSurface,
            )
            Text(
                text = "Inspect mọi giá trị CMS đẩy về SDK runtime cho game này",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
            )
        }
        VLButton(
            text = "Refresh",
            onClick = onRefresh,
            kind = VLButtonKind.Secondary,
            fullWidth = false,
            compact = true,
        )
    }
}

@Composable
private fun StatusSection(ready: Boolean, config: SdkConfig?) {
    Card(title = "STATUS", icon = Icons.Filled.Info) {
        ConfigKVRow(
            label = "Loaded",
            value = if (ready) "YES" else "NO",
            valueColor = if (ready) VLColor.Success else VLColor.Error,
        )
        SectionDivider()
        ConfigKVRow(
            label = "Client ID",
            value = VLPlayString.CLIENT_ID.ifEmpty { "—" },
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "SDK min version",
            value = config?.sdkMinVersion?.takeIf { it.isNotEmpty() } ?: "—",
        )
    }
}

@Composable
private fun FeaturesSection() {
    Card(title = "FEATURES (CMS toggles)", icon = Icons.Filled.Tune) {
        FeatureToggleRow("Identity Verification", SdkConfig.FEATURE_IDENTITY_VERIFICATION)
        SectionDivider()
        FeatureToggleRow("Anti-Addiction", SdkConfig.FEATURE_ANTI_ADDICTION)
        SectionDivider()
        FeatureToggleRow("Guest Login", SdkConfig.FEATURE_GUEST_LOGIN)
        SectionDivider()
        FeatureToggleRow("AppsFlyer Tracking", SdkConfig.FEATURE_APPSFLYER_TRACKING)
        SectionDivider()
        FeatureToggleRow("OTP Required", SdkConfig.FEATURE_OTP_REQUIRED)
        SectionDivider()
        FeatureToggleRow("Email Verification", SdkConfig.FEATURE_EMAIL_VERIFICATION)
    }
}

@Composable
private fun FeatureToggleRow(name: String, key: String) {
    val enabled = VLPlaySDKManager.isFeatureEnabled(key)
    Row(
        modifier = Modifier.padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.CheckCircle else Icons.Filled.Block,
            contentDescription = null,
            tint = if (enabled) VLColor.Success else VLColor.OutlineVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = VLFont.bodyMd, color = VLColor.OnSurface)
            Text(
                text = key,
                style = VLFont.bodySm.copy(fontFamily = FontFamily.Monospace),
                color = VLColor.OnSurfaceVariant,
            )
        }
        Text(
            text = if (enabled) "ON" else "OFF",
            style = VLFont.labelBold,
            color = if (enabled) VLColor.Success else VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun PaymentMethodsSection() {
    val enabledList = remember { VLPlaySDKManager.getEnabledPaymentMethods() }
    Card(
        title = "PAYMENT METHODS",
        icon = Icons.Filled.AttachMoney,
        subtitle = if (enabledList.isEmpty()) "Chưa có method nào bật" else "${enabledList.size} method bật",
    ) {
        PaymentRow("Apple IAP", SdkConfig.PAYMENT_METHOD_APPLE_IAP)
        SectionDivider()
        PaymentRow("Google Play", SdkConfig.PAYMENT_METHOD_GOOGLE_PLAY)
        SectionDivider()
        PaymentRow("AppotaPay", SdkConfig.PAYMENT_METHOD_APPOTAPAY)
        SectionDivider()
        PaymentRow("External", SdkConfig.PAYMENT_METHOD_EXTERNAL)
    }
}

@Composable
private fun PaymentRow(name: String, id: String) {
    val enabled = VLPlaySDKManager.isPaymentMethodEnabled(id)
    Row(
        modifier = Modifier.padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.CheckCircle else Icons.Filled.Block,
            contentDescription = null,
            tint = if (enabled) VLColor.Success else VLColor.OutlineVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = name,
            style = VLFont.bodyMd,
            color = VLColor.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = id,
            style = VLFont.bodySm.copy(fontFamily = FontFamily.Monospace),
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun SupportSection(config: SdkConfig?, context: Context, env: SDKEnvironment) {
    Card(title = "SUPPORT", icon = Icons.Filled.Phone) {
        SupportContactRow(
            label = "Hotline",
            value = config?.hotline ?: "",
            icon = Icons.Filled.Phone,
            onTap = { value ->
                val digits = value.filter { it.isDigit() || it == '+' }
                if (digits.isEmpty()) return@SupportContactRow
                openUri(context, env, "tel:$digits")
            },
        )
        SectionDivider()
        SupportContactRow(
            label = "Email",
            value = config?.emailSupport ?: "",
            icon = Icons.Filled.Email,
            onTap = { value -> openUri(context, env, "mailto:$value") },
        )
        SectionDivider()
        SupportContactRow(
            label = "Fanpage",
            value = config?.fanpage ?: "",
            icon = Icons.Filled.Public,
            onTap = { value ->
                val url = if (value.startsWith("http")) value else "https://$value"
                openUri(context, env, url)
            },
        )
    }
}

@Composable
private fun SupportContactRow(
    label: String,
    value: String,
    icon: ImageVector,
    onTap: (String) -> Unit,
) {
    val isEmpty = value.isEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isEmpty) Modifier else Modifier.clickable { onTap(value) })
            .padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isEmpty) VLColor.OnSurfaceVariant else VLColor.Primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (isEmpty) "—" else value,
            style = if (isEmpty) VLFont.bodyMd else VLFont.bodyMd.copy(fontWeight = FontWeight.Medium),
            color = if (isEmpty) VLColor.OnSurfaceVariant else VLColor.Primary,
        )
    }
}

@Composable
private fun TrackingIdsSection(config: SdkConfig?) {
    // SDKCONFIG-APPSFLYER-PER-PLATFORM (BE df5dd48, plan LOCKED 2026-05-15):
    // BE now ships 4 per-platform fields alongside the legacy flat pair.
    // Android reads `appsflyerAndroidDevKey` / `appsflyerAndroidAppId` first;
    // the iOS pair is exposed here for cross-platform CMS audit only — Android
    // SDK never initialises with iOS keys (no cross-platform fallback, §5.1).
    Card(title = "TRACKING IDs (AppsFlyer)", icon = Icons.Filled.Build) {
        ConfigKVRow(
            label = "Android dev key (used)",
            value = config?.appsflyerAndroidDevKey?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "Android app id (used)",
            value = config?.appsflyerAndroidAppId?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "iOS dev key (audit only)",
            value = config?.appsflyerIosDevKey?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "iOS app id (audit only)",
            value = config?.appsflyerIosAppId?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "legacy flat devKey",
            value = config?.appsflyerDevKey?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
        SectionDivider()
        ConfigKVRow(
            label = "legacy flat appId",
            value = config?.appsflyerAppId?.takeIf { it.isNotEmpty() } ?: "—",
            mono = true,
        )
    }
}

@Composable
private fun AntiAddictionSection(aaConfig: AntiAddictionConfig?) {
    Card(title = "ANTI-ADDICTION (Decree 147/2024)", icon = Icons.Filled.Shield) {
        if (aaConfig == null) {
            Text(
                text = "Chưa nhận được config từ server (cần đăng nhập + bật antiAddiction).",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
                modifier = Modifier.padding(vertical = VLSpacing.xs),
            )
            return@Card
        }
        ConfigKVRow(
            label = "Trạng thái",
            value = if (aaConfig.isEnabled) "Bật" else "Tắt",
            valueColor = if (aaConfig.isEnabled) VLColor.Success else VLColor.OnSurfaceVariant,
        )
        SectionDivider()
        ConfigKVRow(
            label = "Tuổi <16: tối đa/ngày",
            value = minutesText(aaConfig.underageMaxMinutesPerDay),
        )
        ConfigKVRow(
            label = "Tuổi <16: tối đa/phiên",
            value = minutesText(aaConfig.underageMaxMinutesPerSession),
        )
        SectionDivider()
        ConfigKVRow(
            label = "Tuổi 16–17: tối đa/ngày",
            value = minutesText(aaConfig.teenMaxMinutesPerDay),
        )
        ConfigKVRow(
            label = "Tuổi 16–17: tối đa/phiên",
            value = minutesText(aaConfig.teenMaxMinutesPerSession),
        )
        SectionDivider()
        ConfigKVRow(
            label = "Tuổi ≥18",
            value = if (aaConfig.adultMaxMinutesPerDay < 0) "Không giới hạn"
            else minutesText(aaConfig.adultMaxMinutesPerDay),
        )
        SectionDivider()
        ConfigKVRow(
            label = "Polling status mỗi",
            value = "${aaConfig.warningIntervalMinutes} phút",
        )
        SectionDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = VLSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Curfew",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            VLStatePill(
                text = if (aaConfig.isCurfewEnabled) "ENABLED" else "DISABLED",
                tone = if (aaConfig.isCurfewEnabled) VLPillTone.Warning else VLPillTone.Neutral,
            )
        }
        if (aaConfig.isCurfewEnabled) {
            ConfigKVRow(
                label = "Khung giờ",
                value = "%02d:00 – %02d:00".format(aaConfig.curfewStartHour, aaConfig.curfewEndHour),
            )
            if (aaConfig.curfewAppliesTo.isNotEmpty()) {
                ConfigKVRow(
                    label = "Áp dụng",
                    value = aaConfig.curfewAppliesTo.joinToString(", "),
                )
            }
        }
    }
}

@Composable
private fun RawSnapshotSection(config: SdkConfig?) {
    val raw = remember(config) {
        config?.toJson()?.let { prettyJson(it) } ?: "(chưa có config)"
    }
    Card(title = "RAW SNAPSHOT", icon = Icons.Filled.Settings, subtitle = "SdkConfig.toJson() pretty-printed") {
        Text(
            text = raw,
            style = VLFont.codeMd,
            color = VLColor.OnSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = VLSpacing.xs),
        )
    }
}

@Composable
private fun Card(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    content: @Composable () -> Unit,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VLSpacing.xs),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VLColor.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = VLFont.labelBold,
                        color = VLColor.OnSurfaceVariant,
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = VLFont.bodySm,
                            color = VLColor.OnSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun ConfigKVRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = VLColor.OnSurface,
    mono: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (mono) {
                VLFont.bodyMd.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
            } else {
                VLFont.bodyMd.copy(fontWeight = FontWeight.Medium)
            },
            color = valueColor,
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        color = VLColor.OutlineVariant.copy(alpha = 0.4f),
    )
}

private fun minutesText(m: Int): String {
    if (m < 0) return "—"
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

private fun openUri(context: Context, env: SDKEnvironment, uri: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        env.toast.error("Không mở được", e.message ?: uri)
    }
}

private fun prettyJson(raw: String): String {
    return try {
        val obj = JSONObject(raw)
        obj.toString(2)
    } catch (e: Exception) {
        raw
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RemoteConfigScreenPreview() {
    VLTheme {
        RemoteConfigScreen(onBack = {})
    }
}
