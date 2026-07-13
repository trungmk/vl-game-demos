package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONObject
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLGiftcodeInput
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Giftcode redemption screen. Parity iOS demo2 [GiftcodeRedeemView].
 * Wires [VLPlaySDKManager.redeemGiftcode] → `POST /api/v1/giftcode/redeem`.
 *
 * Recent list (capped at 20) persists across nav via SharedPreferences key
 * `demo.giftcode.recent` — same store-key shape as iOS UserDefaults entry.
 * Stored as JSON `[{code, reward, datetime, status}]`.
 */
@Composable
fun GiftcodeRedeemScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val store = remember(context) { GiftcodeRecentStore(context.applicationContext) }

    var rawCode by rememberSaveable { mutableStateOf("") }
    var recent by remember { mutableStateOf(store.loadAll()) }
    var lastResult by remember { mutableStateOf<RedeemResult?>(null) }

    val isSignedIn = env.currentUser?.let {
        it.accessTokenLength > 0 && it.accountId.isNotEmpty()
    } == true
    val canSubmit = isSignedIn && rawCode.length >= MIN_LEN

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Giftcode")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Text(
                    text = "Đổi Giftcode",
                    style = VLFont.h1,
                    color = VLColor.OnSurface,
                )
                Text(
                    text = "Nhập mã $MIN_LEN–$MAX_LEN ký tự để nhận quà.",
                    style = VLFont.bodyMd,
                    color = VLColor.OnSurfaceVariant,
                )

                if (!isSignedIn) SignInGuard()

                RedeemCard(
                    rawCode = rawCode,
                    onRawCodeChange = { rawCode = it },
                    canSubmit = canSubmit,
                    onSubmit = {
                        redeem(
                            activity = activity,
                            env = env,
                            code = rawCode,
                            onSuccess = { reward ->
                                val entry = RecentEntry(
                                    code = rawCode,
                                    reward = reward,
                                    datetime = nowString(),
                                    status = RecentStatus.Verified,
                                )
                                recent = (listOf(entry) + recent).take(MAX_RECENT)
                                store.saveAll(recent)
                                lastResult = RedeemResult(
                                    code = rawCode,
                                    success = true,
                                    message = "Mã ${rawCode} đã được đổi. $reward",
                                )
                                rawCode = ""
                            },
                            onFailure = { message ->
                                val entry = RecentEntry(
                                    code = rawCode,
                                    reward = message,
                                    datetime = nowString(),
                                    status = RecentStatus.Failed,
                                )
                                recent = (listOf(entry) + recent).take(MAX_RECENT)
                                store.saveAll(recent)
                                lastResult = RedeemResult(
                                    code = rawCode,
                                    success = false,
                                    message = message,
                                )
                            },
                        )
                    },
                )

                lastResult?.let { result ->
                    ResultCard(result = result, onDismiss = { lastResult = null })
                }

                RecentSection(
                    recent = recent,
                    onClearAll = {
                        recent = emptyList()
                        store.saveAll(emptyList())
                    },
                )

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
private fun SignInGuard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.WarningContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, VLColor.Warning.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = VLColor.Warning,
            )
            Text(
                text = "Cần đăng nhập để đổi giftcode.",
                style = VLFont.bodyMd,
                color = VLColor.OnSurface,
            )
        }
    }
}

@Composable
private fun RedeemCard(
    rawCode: String,
    onRawCodeChange: (String) -> Unit,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            // Decorative gradient bar — parity iOS rainbow header.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFFD93D),
                                Color(0xFF6BCB77),
                                Color(0xFF4D96FF),
                            ),
                        ),
                    ),
            )

            Text(
                text = "MÃ GIFTCODE",
                style = VLFont.labelBold,
                color = VLColor.OnSurfaceVariant,
            )

            VLGiftcodeInput(
                rawCode = rawCode,
                onRawCodeChange = onRawCodeChange,
                maxLength = MAX_LEN,
                placeholder = "VD: TEST001",
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = VLColor.OnSurfaceVariant,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = "${rawCode.length} / $MAX_LEN ký tự",
                    style = VLFont.bodySm,
                    color = VLColor.OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (rawCode.isNotEmpty()) {
                    TextButton(onClick = { onRawCodeChange("") }) {
                        Text(
                            text = "Xoá",
                            style = VLFont.labelMd,
                            color = VLColor.Primary,
                        )
                    }
                }
            }

            VLButton(
                text = "Đổi quà",
                onClick = onSubmit,
                kind = VLButtonKind.Primary,
                enabled = canSubmit,
            )
        }
    }
}

@Composable
private fun ResultCard(result: RedeemResult, onDismiss: () -> Unit) {
    val tone = if (result.success) VLColor.Success else VLColor.Error
    val title = if (result.success) "Đổi mã thành công" else "Đổi mã thất bại"
    val icon = if (result.success) Icons.Filled.CheckCircle else Icons.Filled.Warning

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = tone.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tone,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = VLFont.labelBold,
                        color = VLColor.OnSurface,
                    )
                    Text(
                        text = "Mã: ${result.code}",
                        style = VLFont.codeMd,
                        color = VLColor.OnSurfaceVariant,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = VLColor.OnSurfaceVariant,
                    )
                }
            }
            Text(
                text = result.message,
                style = VLFont.bodyMd,
                color = VLColor.OnSurface,
            )
        }
    }
}

@Composable
private fun RecentSection(recent: List<RecentEntry>, onClearAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Lịch sử gần đây",
                style = VLFont.labelBold,
                color = VLColor.OnSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (recent.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text(
                        text = "Xoá hết",
                        style = VLFont.labelBold,
                        color = VLColor.Primary,
                    )
                }
            }
        }

        if (recent.isEmpty()) {
            EmptyRecent()
        } else {
            RecentList(recent)
        }
    }
}

@Composable
private fun EmptyRecent() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Archive,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = "Chưa có lượt đổi nào",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentList(recent: List<RecentEntry>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(modifier = Modifier.padding(VLSpacing.md)) {
            recent.forEachIndexed { idx, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VLSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
                ) {
                    Icon(
                        imageVector = if (entry.status == RecentStatus.Verified)
                            Icons.Filled.Archive
                        else
                            Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (entry.status == RecentStatus.Verified)
                            VLColor.Primary
                        else
                            VLColor.Error,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.code,
                            style = VLFont.codeMd,
                            color = VLColor.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = entry.datetime,
                            style = VLFont.bodySm,
                            color = VLColor.OnSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = entry.reward,
                            style = VLFont.bodyMd,
                            color = VLColor.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        VLStatePill(
                            text = if (entry.status == RecentStatus.Verified) "VERIFIED" else "FAILED",
                            tone = if (entry.status == RecentStatus.Verified)
                                VLPillTone.Neutral else VLPillTone.Error,
                        )
                    }
                }
                if (idx != recent.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 36.dp),
                        color = VLColor.OutlineVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

private fun redeem(
    activity: Activity?,
    env: SDKEnvironment,
    code: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang đổi mã…")
    VLPlaySDKManager.redeemGiftcode(activity, code, object : VLPlaySDKManager.GiftcodeRedeemListener {
        override fun onSuccess(rewardData: JSONObject) {
            env.loading.hide()
            val reward = rewardLabel(rewardData)
            env.toast.success("Đổi mã thành công", reward)
            onSuccess(reward)
        }

        override fun onFailure(message: String?, errorCode: Int) {
            env.loading.hide()
            val msg = message ?: "Đổi mã thất bại (mã lỗi $errorCode)"
            env.toast.error("Đổi mã thất bại", msg)
            onFailure(msg)
        }
    })
}

private fun rewardLabel(data: JSONObject): String {
    data.optString("reward").takeIf { it.isNotEmpty() }?.let { return it }
    val type = data.optString("type")
    if (type.isNotEmpty()) return "Loại: $type"
    return "Phần thưởng đã ghi nhận"
}

private fun nowString(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private const val MIN_LEN = 4
private const val MAX_LEN = 50
private const val MAX_RECENT = 20

private data class RedeemResult(val code: String, val success: Boolean, val message: String)

private data class RecentEntry(
    val code: String,
    val reward: String,
    val datetime: String,
    val status: RecentStatus,
)

private enum class RecentStatus { Verified, Failed }

private class GiftcodeRecentStore(context: Context) {
    private val prefs = context.getSharedPreferences("demo.giftcode", Context.MODE_PRIVATE)

    fun loadAll(): List<RecentEntry> {
        val raw = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                RecentEntry(
                    code = o.optString("code"),
                    reward = o.optString("reward"),
                    datetime = o.optString("datetime"),
                    status = if (o.optString("status") == "Verified") RecentStatus.Verified else RecentStatus.Failed,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(list: List<RecentEntry>) {
        val arr = org.json.JSONArray()
        list.forEach { e ->
            val o = JSONObject()
            o.put("code", e.code)
            o.put("reward", e.reward)
            o.put("datetime", e.datetime)
            o.put("status", e.status.name)
            arr.put(o)
        }
        prefs.edit().putString(KEY_RECENT, arr.toString()).apply()
    }

    private companion object {
        const val KEY_RECENT = "recent_v1"
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GiftcodeRedeemScreenPreview() {
    VLTheme {
        GiftcodeRedeemScreen(onBack = {})
    }
}
