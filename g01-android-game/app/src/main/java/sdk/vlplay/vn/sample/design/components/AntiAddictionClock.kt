package sdk.vlplay.vn.sample.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Pure-UI render state for [AntiAddictionClock]. Phase 4 [SDKEnvironment]
 * derives this from [AntiAddictionManager.getLastStatus]; this component
 * stays decoupled from the SDK so previews + tests don't need a session.
 *
 * @param ageGroup raw value from server (`underage`/`teen`/`adult`/`""`).
 *        Empty string hides the badge row.
 *
 * Mapping iOS↔Android (post Day 26 parity):
 * - `shouldKick` / `shouldWarn`: read from BE-computed status fields on
 *   both platforms (previously Android derived locally).
 * - `currentSessionMinutes` / `remainingSessionMinutes`: parsed from BE
 *   status on both platforms.
 */
data class AntiAddictionClockState(
    val hasStatus: Boolean,
    val remainingSessionSeconds: Int,
    val currentSessionMinutes: Int,
    val remainingTodayMinutes: Int,
    val ageGroup: String,
    val curfewActive: Boolean,
    val shouldWarn: Boolean,
    val shouldKick: Boolean,
) {
    companion object {
        val Empty = AntiAddictionClockState(
            hasStatus = false,
            remainingSessionSeconds = 0,
            currentSessionMinutes = 0,
            remainingTodayMinutes = 0,
            ageGroup = "",
            curfewActive = false,
            shouldWarn = false,
            shouldKick = false,
        )
    }
}

/**
 * Circular countdown clock for anti-addiction session. Parity iOS [AntiAddictionClock].
 * Center text shows HH:mm:ss; ring fills as session elapses (clockwise from 12 o'clock).
 *
 * Caller must drive ticks by re-invoking with updated [state].
 */
@Composable
fun AntiAddictionClock(
    state: AntiAddictionClockState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val tone = when {
        state.shouldKick -> VLColor.Error
        state.shouldWarn -> VLColor.Warning
        else -> VLColor.Primary
    }

    val totalSeconds = state.currentSessionMinutes * 60 + state.remainingSessionSeconds
    val rawProgress = if (totalSeconds > 0) {
        (state.currentSessionMinutes * 60).toFloat() / totalSeconds.toFloat()
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "AAProgress",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, tone.copy(alpha = 0.35f)),
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            Ring(
                progress = animatedProgress,
                tone = tone,
                centerLine1 = if (state.hasStatus) formatHms(state.remainingSessionSeconds) else "--:--:--",
                centerLine2 = if (state.hasStatus) "còn lại" else "đang tải",
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusBadge(text = badgeText(state), tone = tone)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = if (state.hasStatus) "Hôm nay còn" else "Phiên chơi",
                        style = MaterialTheme.typography.labelMedium,
                        color = VLColor.OnSurfaceVariant,
                    )
                    Text(
                        text = if (state.hasStatus) formatRemainingToday(state.remainingTodayMinutes)
                        else "Chống nghiện ON",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = VLColor.OnSurface,
                    )
                }

                if (state.hasStatus && state.ageGroup.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Parity iOS VLIcon.badge = "person.crop.circle.fill"
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = VLColor.OnSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = ageGroupLabel(state.ageGroup),
                            style = MaterialTheme.typography.bodySmall,
                            color = VLColor.OnSurfaceVariant,
                        )
                    }
                }

                if (state.curfewActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NightsStay,
                            contentDescription = null,
                            tint = VLColor.Error,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "Đang trong giờ cấm",
                            style = MaterialTheme.typography.bodySmall,
                            color = VLColor.Error,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(0.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun Ring(
    progress: Float,
    tone: Color,
    centerLine1: String,
    centerLine2: String,
) {
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            // Track
            drawArc(
                color = tone.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            // Fill (clockwise from 12 o'clock)
            drawArc(
                color = tone,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLine1,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                ),
                color = VLColor.OnSurface,
            )
            Text(
                text = centerLine2,
                style = MaterialTheme.typography.bodySmall,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, tone: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(tone.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tone),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            ),
            color = tone,
        )
    }
}

private fun badgeText(state: AntiAddictionClockState): String = when {
    !state.hasStatus -> "ĐANG CHỜ SERVER"
    state.shouldKick -> "HẾT GIỜ"
    state.shouldWarn -> "NÊN NGHỈ"
    else -> "ĐANG GIÁM SÁT"
}

private fun ageGroupLabel(ageGroup: String): String = when (ageGroup) {
    "underage" -> "Dưới 16 tuổi"
    "teen" -> "16–17 tuổi"
    "adult" -> "Từ 18 tuổi"
    else -> if (ageGroup.isEmpty()) "—" else ageGroup
}

private fun formatHms(seconds: Int): String {
    val s = maxOf(0, seconds)
    return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
}

private fun formatRemainingToday(minutes: Int): String {
    val m = maxOf(0, minutes)
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun AntiAddictionClockPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            // No status yet
            AntiAddictionClock(state = AntiAddictionClockState.Empty)

            // Healthy: 30/60 minute session, 2h remaining today
            AntiAddictionClock(
                state = AntiAddictionClockState(
                    hasStatus = true,
                    remainingSessionSeconds = 30 * 60,
                    currentSessionMinutes = 30,
                    remainingTodayMinutes = 120,
                    ageGroup = "adult",
                    curfewActive = false,
                    shouldWarn = false,
                    shouldKick = false,
                ),
                onClick = {},
            )

            // Warning: 5 minutes left
            AntiAddictionClock(
                state = AntiAddictionClockState(
                    hasStatus = true,
                    remainingSessionSeconds = 5 * 60,
                    currentSessionMinutes = 55,
                    remainingTodayMinutes = 5,
                    ageGroup = "teen",
                    curfewActive = false,
                    shouldWarn = true,
                    shouldKick = false,
                ),
            )

            // Kicked + curfew
            AntiAddictionClock(
                state = AntiAddictionClockState(
                    hasStatus = true,
                    remainingSessionSeconds = 0,
                    currentSessionMinutes = 60,
                    remainingTodayMinutes = 0,
                    ageGroup = "underage",
                    curfewActive = true,
                    shouldWarn = false,
                    shouldKick = true,
                ),
            )
        }
    }
}
