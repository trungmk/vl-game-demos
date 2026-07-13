package sdk.vlplay.vn.sample.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.config.SdkConfig
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.tracking.AppsFlyerHelper
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * AppsFlyer Events test console — fire any CMS-configured AppsFlyer event
 * through the SDK's dynamic resolver. Parity iOS demo2 [AppsFlyerEventsView].
 *
 * Reads `afEvents` from [VLPlaySDKManager.getCurrentConfig] (the top-level
 * CMS dynamic layer: `map` stableKey→afName + `disabled[]`). Firing passes the
 * STABLE KEY to [AppsFlyerHelper.logSimpleEvent], so the SDK's resolver does
 * the rename / drop and honors the `features.appsFlyerTracking` master toggle —
 * exactly the runtime path a production game's events take. When CMS hasn't
 * configured afEvents, falls back to a built-in canonical AF event seed so the
 * console still works.
 *
 * Reached from the MainHub "AF Events" tile.
 */
@Composable
fun AppsFlyerEventsScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    var refreshTick by remember { mutableStateOf(0) }
    val config = remember(refreshTick) { VLPlaySDKManager.getCurrentConfig() }
    val trackingEnabled = remember(refreshTick) {
        VLPlaySDKManager.isFeatureEnabled(SdkConfig.FEATURE_APPSFLYER_TRACKING)
    }
    val afEnabled = config?.isAfEventsEnabled ?: false
    val built = remember(refreshTick) { buildEvents(config) }
    val events = built.first
    val usingFallback = built.second

    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val params = remember { mutableStateListOf<AfParamRow>() }
    var lastFired by remember { mutableStateOf<String?>(null) }

    fun resetParamsFor(stableKey: String) {
        params.clear()
        defaultParams(stableKey).forEach { (k, v) -> params.add(AfParamRow(k, v)) }
    }

    // A stale selection (event vanished after a refresh) simply renders nothing:
    // the `selectedEvent != null` guard below hides the editor until re-picked.
    // (No state write during composition — re-selecting refills params.)
    val selectedEvent = events.firstOrNull { it.stableKey == selectedKey }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "AppsFlyer Events")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AppsFlyer Events", style = VLFont.h1, color = VLColor.OnSurface)
                        Text(
                            "Fire event từ CMS afEvents qua SDK resolver",
                            style = VLFont.bodyMd,
                            color = VLColor.OnSurfaceVariant,
                        )
                    }
                    VLButton(
                        text = "Refresh",
                        onClick = {
                            refreshTick++
                            env.toast.info("Đã refresh", "Đọc lại afEvents từ SdkConfig")
                        },
                        kind = VLButtonKind.Secondary,
                        fullWidth = false,
                        compact = true,
                    )
                }

                // Status
                Card(title = "TRẠNG THÁI") {
                    StatusRow("afEvents layer (CMS)", if (afEnabled) "ON" else "OFF", afEnabled)
                    StatusRow("appsFlyerTracking (master)", if (trackingEnabled) "ON" else "OFF", trackingEnabled)
                    StatusRow(
                        "Số event",
                        "${events.size}" + if (usingFallback) " (built-in)" else " (CMS)",
                        !usingFallback,
                    )
                    if (usingFallback) {
                        Text(
                            "CMS afEvents trống → dùng danh sách mẫu built-in. Cấu hình afEvents trong CMS để thấy event thật.",
                            style = VLFont.bodySm,
                            color = VLColor.OnSurfaceVariant,
                        )
                    } else if (!trackingEnabled) {
                        Text(
                            "Master tracking OFF → SDK short-circuit, event sẽ không thực sự gửi đi.",
                            style = VLFont.bodySm,
                            color = VLColor.Warning,
                        )
                    }
                }

                // Event picker
                Card(title = "CHỌN EVENT") {
                    if (events.isEmpty()) {
                        Text("Không có event nào.", style = VLFont.bodyMd, color = VLColor.OnSurfaceVariant)
                    } else {
                        events.forEach { ev ->
                            EventRow(
                                event = ev,
                                selected = ev.stableKey == selectedKey,
                                onClick = {
                                    selectedKey = ev.stableKey
                                    resetParamsFor(ev.stableKey)
                                },
                            )
                        }
                    }
                }

                // Param editor + send (only when an event is selected)
                if (selectedEvent != null) {
                    val ev = selectedEvent
                    Card(title = "PARAMS — ${ev.stableKey}") {
                        if (params.isEmpty()) {
                            Text(
                                "Chưa có param. Bấm “Thêm param”.",
                                style = VLFont.bodySm,
                                color = VLColor.OnSurfaceVariant,
                            )
                        }
                        params.forEach { row ->
                            key(row) {
                                ParamRowEditor(row = row, onDelete = { params.remove(row) })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { params.add(AfParamRow("", "")) }) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = VLColor.Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(4.dp))
                                Text("Thêm param", style = VLFont.labelMd, color = VLColor.Primary)
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { resetParamsFor(ev.stableKey) }) {
                                Text("Mặc định", style = VLFont.labelMd, color = VLColor.OnSurfaceVariant)
                            }
                        }
                    }

                    VLButton(
                        text = if (ev.disabled) "Gửi (sẽ bị drop)" else "Gửi event",
                        onClick = {
                            val values = HashMap<String, Any>()
                            params.forEach { row ->
                                val k = row.key.trim()
                                if (k.isNotEmpty()) values[k] = coerce(row.value)
                            }
                            AppsFlyerHelper.logSimpleEvent(ev.stableKey, values)

                            val paramDesc = if (values.isEmpty()) "(no params)"
                            else values.entries.map { "${it.key}=${it.value}" }.sorted().joinToString(", ")
                            lastFired = when {
                                ev.disabled -> {
                                    env.toast.warning("Event disabled", "${ev.stableKey} bị disable trong CMS → resolver drop, không gửi.")
                                    "${ev.stableKey} → DROPPED (disabled) · $paramDesc"
                                }
                                !trackingEnabled -> {
                                    env.toast.warning("Tracking OFF", "features.appsFlyerTracking = OFF → SDK short-circuit.")
                                    "${ev.stableKey} → ${ev.afName} · master OFF · $paramDesc"
                                }
                                else -> {
                                    env.toast.success("Đã fire", "${ev.stableKey} → ${ev.afName}")
                                    "${ev.stableKey} → ${ev.afName} · $paramDesc"
                                }
                            }
                        },
                        kind = if (ev.disabled) VLButtonKind.Secondary else VLButtonKind.Primary,
                    )
                }

                lastFired?.let {
                    Card(title = "LẦN GỬI GẦN NHẤT") {
                        Text(it, style = VLFont.code, color = VLColor.OnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(VLSpacing.md))
                VLButton(text = "← Quay lại", onClick = onBack, kind = VLButtonKind.Ghost)
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

@Composable
private fun StatusRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = VLFont.bodyMd, color = VLColor.OnSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            value,
            style = VLFont.bodySm.copy(fontWeight = FontWeight.Bold),
            color = if (ok) VLColor.Success else VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun EventRow(event: AfEventItem, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) VLColor.Primary else VLColor.OutlineVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.stableKey,
                style = VLFont.codeMd.copy(fontFamily = FontFamily.Monospace),
                color = VLColor.OnSurface,
            )
            if (event.afName != event.stableKey) {
                Text(
                    "→ ${event.afName}",
                    style = VLFont.code.copy(fontFamily = FontFamily.Monospace),
                    color = VLColor.Tertiary,
                )
            }
        }
        if (event.disabled) {
            Text(
                "DISABLED",
                style = VLFont.labelBold,
                color = VLColor.Error,
                modifier = Modifier
                    .background(VLColor.ErrorContainer, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun ParamRowEditor(row: AfParamRow, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        OutlinedTextField(
            value = row.key,
            onValueChange = { row.key = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = VLFont.code,
            placeholder = { Text("key", style = VLFont.code, color = VLColor.OnSurfaceVariant) },
        )
        OutlinedTextField(
            value = row.value,
            onValueChange = { row.value = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = VLFont.code,
            placeholder = { Text("value", style = VLFont.code, color = VLColor.OnSurfaceVariant) },
        )
        Icon(
            imageVector = Icons.Filled.RemoveCircle,
            contentDescription = "Xoá param",
            tint = VLColor.Error,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
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
            Text(title, style = VLFont.labelBold, color = VLColor.OnSurfaceVariant)
            content()
        }
    }
}

// MARK: - Model + helpers

private data class AfEventItem(
    val stableKey: String,   // resolver input — what we fire
    val afName: String,      // resolver output — CMS-mapped name (== stableKey if no remap)
    val disabled: Boolean,   // in afEvents.disabled → resolver drops it
    val fromCMS: Boolean,    // false = built-in fallback seed
)

private class AfParamRow(key: String, value: String) {
    var key by mutableStateOf(key)
    var value by mutableStateOf(value)
}

/** Build the event list from the SDK config's afEvents layer; fall back to a
 *  built-in canonical seed when CMS hasn't configured any. Returns (list, usingFallback). */
private fun buildEvents(config: SdkConfig?): Pair<List<AfEventItem>, Boolean> {
    val map: Map<String, String> = config?.afEventsMap ?: emptyMap()
    val disabled: List<String> = config?.afEventsDisabled ?: emptyList()
    val items = mutableListOf<AfEventItem>()
    val seen = mutableSetOf<String>()
    map.toSortedMap().forEach { (k, v) ->
        items.add(AfEventItem(k, v, disabled.contains(k), true))
        seen.add(k)
    }
    disabled.sorted().forEach { d ->
        if (seen.add(d)) items.add(AfEventItem(d, d, true, true))
    }
    return if (items.isEmpty()) {
        FALLBACK_EVENTS.map { AfEventItem(it, it, false, false) } to true
    } else {
        items.toList() to false
    }
}

/** String → number coercion so af_revenue=100000 sends as a number not "100000". */
private fun coerce(s: String): Any {
    val t = s.trim()
    t.toIntOrNull()?.let { return it }
    t.toDoubleOrNull()?.let { return it }
    return s
}

/** Recommended default params per common AppsFlyer event; generic single row
 *  for anything else. All editable / removable in the UI. */
private fun defaultParams(stableKey: String): List<Pair<String, String>> = when (stableKey) {
    "af_purchase", "af_payment_success" -> listOf(
        "af_revenue" to "100000",
        "af_currency" to "VND",
        "af_content_id" to "pack_001",
        "af_quantity" to "1",
    )
    "af_add_to_cart", "af_initiated_checkout" -> listOf(
        "af_revenue" to "100000",
        "af_currency" to "VND",
        "af_content_id" to "pack_001",
    )
    "af_login", "af_complete_registration" -> listOf("af_registration_method" to "email")
    "af_content_view" -> listOf("af_content_id" to "item_001", "af_content_type" to "product")
    "af_level_achieved" -> listOf("af_level" to "2")
    else -> listOf("source" to "demo_console")
}

private val FALLBACK_EVENTS = listOf(
    "af_login",
    "af_complete_registration",
    "af_purchase",
    "af_content_view",
    "af_level_achieved",
    "af_add_to_cart",
    "af_initiated_checkout",
    "af_tutorial_completion",
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AppsFlyerEventsScreenPreview() {
    VLTheme {
        AppsFlyerEventsScreen(onBack = {})
    }
}
