package sdk.vlplay.vn.sample.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.antiaddiction.AntiAddictionConfig
import sdk.vlplay.vn.antiaddiction.AntiAddictionManager
import sdk.vlplay.vn.antiaddiction.AntiAddictionStatus
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLPillTone
import sdk.vlplay.vn.sample.design.components.VLProgressBar
import sdk.vlplay.vn.sample.design.components.VLPropertyRow
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

/**
 * Anti-Addiction status + demo controls. Parity iOS demo2 [AntiAddictionView].
 *
 * Reads from [AntiAddictionManager.getLastStatus] / `getConfig` / `isSessionActive`
 * (the same source [SDKEnvironment.antiAddictionClockState] tracks). 4 cards
 * when a status is available (session / today / identity / curfew) plus the
 * always-visible config card + 2 demo controls (force-warn / force-kick).
 *
 */
@Composable
fun AntiAddictionDebugScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    var refreshTick by remember { mutableStateOf(0) }
    val status = remember(refreshTick) { AntiAddictionManager.getLastStatus() }
    val config = remember(refreshTick) { AntiAddictionManager.getConfig() }
    val sessionActive = remember(refreshTick) { AntiAddictionManager.isSessionActive() }
    val clockState = env.antiAddictionClockState

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Anti-Addiction")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Header(
                    sessionActive = sessionActive,
                    onRefresh = {
                        AntiAddictionManager.checkStatusNow()
                        refreshTick++
                        env.toast.info("Đang cập nhật trạng thái…")
                    },
                )

                if (status == null) {
                    EmptyState(config = config)
                } else {
                    if (clockState.shouldKick) KickBanner()
                    else if (clockState.shouldWarn) WarnBanner(currentSessionMinutes = status.currentSessionMinutes)

                    SessionCard(status = status, clockState = env.antiAddictionClockState)
                    TodayCard(status = status)
                    IdentityCard(env = env)
                    CurfewCard(config = config, isCurfewActive = status.isCurfew)
                }

                ConfigCard(config = config)
                DemoControlsCard(
                    onForceWarn = {
                        AntiAddictionManager.debugForceWarnPopup()
                        env.toast.info("debug: warn", "AntiAddictionManager.debugForceWarnPopup() invoked")
                        refreshTick++
                    },
                    onForceKick = {
                        AntiAddictionManager.debugForceKickPopup()
                        env.toast.warning("debug: kick", "Sẽ đăng xuất sau khi đóng (mirror flow prod)")
                        refreshTick++
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
private fun Header(sessionActive: Boolean, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Chống nghiện",
            style = VLFont.h1,
            color = VLColor.OnSurface,
        )
        Text(
            text = "Giám sát tuân thủ NĐ 147/2024 (server-driven)",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (sessionActive) VLColor.Success else VLColor.OnSurfaceVariant),
            )
            Text(
                text = if (sessionActive) "Phiên chơi đang được giám sát" else "Phiên chưa khởi động",
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = VLColor.Primary,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(config: AntiAddictionConfig?) {
    val intervalMin = config?.warningIntervalMinutes ?: 30
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
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = "Đang chờ trạng thái từ server…",
                style = VLFont.bodyMd,
                color = VLColor.OnSurface,
            )
            Text(
                text = "SDK gọi /anti-addiction/status sau ~60s đầu phiên, sau đó mỗi $intervalMin phút.",
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WarnBanner(currentSessionMinutes: Int) {
    Banner(
        tone = VLColor.Warning,
        icon = Icons.Filled.Warning,
        title = "Cảnh báo nghỉ ngơi",
        message = "Bạn đã chơi liên tục $currentSessionMinutes phút. Nghỉ ngơi để bảo vệ sức khỏe.",
    )
}

@Composable
private fun KickBanner() {
    Banner(
        tone = VLColor.Error,
        icon = Icons.Filled.Gavel,
        title = "Hết giờ chơi",
        message = "SDK sẽ đăng xuất theo quy định. Vui lòng quay lại sau.",
    )
}

@Composable
private fun Banner(tone: Color, icon: ImageVector, title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = tone.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tone,
                modifier = Modifier.size(22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = VLFont.labelBold, color = VLColor.OnSurface)
                Text(text = message, style = VLFont.bodyMd, color = VLColor.OnSurface)
            }
        }
    }
}

@Composable
private fun SessionCard(
    status: AntiAddictionStatus,
    clockState: sdk.vlplay.vn.sample.design.components.AntiAddictionClockState,
) {
    val totalSeconds = status.currentSessionMinutes * 60 + clockState.remainingSessionSeconds
    val progress = if (totalSeconds > 0) {
        (status.currentSessionMinutes * 60).toFloat() / totalSeconds
    } else 0f
    val remainingFormatted = formatHms(clockState.remainingSessionSeconds)

    Card(title = "PHIÊN HIỆN TẠI", icon = Icons.Filled.Schedule) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = remainingFormatted,
                style = VLFont.h1.copy(fontFamily = FontFamily.Monospace),
                color = VLColor.OnSurface,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "còn lại", style = VLFont.labelMd, color = VLColor.OnSurfaceVariant)
                Text(text = "phiên", style = VLFont.labelBold, color = VLColor.OnSurface)
            }
        }
        VLProgressBar(progress = progress)
        Text(
            text = "Đã chơi ${status.currentSessionMinutes} phút",
            style = VLFont.bodySm,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun TodayCard(status: AntiAddictionStatus) {
    val played = status.totalPlayedMinutesToday.coerceAtLeast(0)
    val remaining = status.remainingMinutesToday.coerceAtLeast(0)
    val total = played + remaining
    val progress = if (total > 0) played.toFloat() / total else 0f
    val remainingText = if (remaining > 0) "Còn ${remaining}m" else "—"

    Card(title = "HÔM NAY", icon = Icons.Filled.CalendarToday) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$played / $total phút",
                style = VLFont.h2,
                color = VLColor.OnSurface,
                modifier = Modifier.weight(1f),
            )
            Text(text = remainingText, style = VLFont.labelBold, color = VLColor.Primary)
        }
        VLProgressBar(progress = progress)
        Text(
            text = "Reset 00:00 giờ Việt Nam",
            style = VLFont.bodySm,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun IdentityCard(env: SDKEnvironment) {
    Card(title = "DANH TÍNH", icon = Icons.Filled.Badge) {
        VLPropertyRow(
            label = "User",
            value = env.currentUser?.accountName?.takeIf { it.isNotEmpty() } ?: "—",
            icon = Icons.Filled.Person,
        )
    }
}

@Composable
private fun CurfewCard(config: AntiAddictionConfig?, isCurfewActive: Boolean) {
    Card(title = "GIỜ CẤM", icon = Icons.Filled.NightsStay) {
        val window = config?.let {
            "%02d:00 – %02d:00".format(it.curfewStartHour, it.curfewEndHour)
        } ?: "—"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = window,
                style = VLFont.h2,
                color = VLColor.OnSurface,
                modifier = Modifier.weight(1f),
            )
            if (isCurfewActive) {
                VLStatePill(text = "ĐANG ÁP DỤNG", tone = VLPillTone.Error, icon = Icons.Filled.Lock)
            } else {
                VLStatePill(text = "KHÔNG", tone = VLPillTone.Neutral)
            }
        }
        config?.curfewAppliesTo?.takeIf { it.isNotEmpty() }?.let { groups ->
            Text(
                text = "Áp dụng: ${groups.joinToString(", ")}",
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConfigCard(config: AntiAddictionConfig?) {
    Card(title = "CẤU HÌNH", icon = Icons.Filled.Settings) {
        if (config == null) {
            Text(
                text = "Chưa nhận được config từ server.",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
            )
            return@Card
        }
        VLPropertyRow(
            label = "Trạng thái",
            value = if (config.isEnabled) "Bật" else "Tắt",
            icon = Icons.Filled.Settings,
        )
        Divider()
        VLPropertyRow(
            label = "Tuổi <16: tối đa/ngày",
            value = minutesText(config.underageMaxMinutesPerDay),
            icon = Icons.Filled.CalendarToday,
        )
        VLPropertyRow(
            label = "Tuổi <16: tối đa/phiên",
            value = minutesText(config.underageMaxMinutesPerSession),
            icon = Icons.Filled.Schedule,
        )
        Divider()
        VLPropertyRow(
            label = "Tuổi 16–17: tối đa/ngày",
            value = minutesText(config.teenMaxMinutesPerDay),
            icon = Icons.Filled.CalendarToday,
        )
        VLPropertyRow(
            label = "Tuổi 16–17: tối đa/phiên",
            value = minutesText(config.teenMaxMinutesPerSession),
            icon = Icons.Filled.Schedule,
        )
        Divider()
        VLPropertyRow(
            label = "Tuổi ≥18",
            value = if (config.adultMaxMinutesPerDay < 0) "Không giới hạn"
            else minutesText(config.adultMaxMinutesPerDay),
            icon = Icons.Filled.CalendarToday,
        )
        Divider()
        VLPropertyRow(
            label = "Polling status mỗi",
            value = "${config.warningIntervalMinutes} phút",
            icon = Icons.Filled.Schedule,
        )
    }
}

@Composable
private fun DemoControlsCard(onForceWarn: () -> Unit, onForceKick: () -> Unit) {
    Card(title = "DEMO CONTROLS", icon = Icons.Filled.Terminal) {
        VLButton(
            text = "Force Warn Popup",
            onClick = onForceWarn,
            kind = VLButtonKind.Secondary,
        )
        VLButton(
            text = "Force Kick Popup (sign out)",
            onClick = onForceKick,
            kind = VLButtonKind.Destructive,
        )
    }
}

@Composable
private fun Card(
    title: String,
    icon: ImageVector,
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
                Text(
                    text = title,
                    style = VLFont.labelBold,
                    color = VLColor.OnSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = VLColor.OutlineVariant.copy(alpha = 0.4f))
}

private fun minutesText(m: Int): String {
    if (m < 0) return "—"
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

private fun formatHms(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AntiAddictionDebugScreenPreview() {
    VLTheme {
        AntiAddictionDebugScreen(onBack = {})
    }
}
