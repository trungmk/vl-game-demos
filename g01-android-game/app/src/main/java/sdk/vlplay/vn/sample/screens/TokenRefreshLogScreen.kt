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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLPillTone
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
import sdk.vlplay.vn.sample.models.TokenRefreshLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Token-refresh observability log. Parity iOS demo2 [TokenRefreshLogView].
 *
 * Reads from [SDKEnvironment.tokenRefreshLog]. Android SDK doesn't currently
 * expose a refresh hook, so the log only fills when something deliberately
 * calls [SDKEnvironment.logTokenRefresh] — matching iOS where the Dashboard
 * "Token Refresh" tile would do that. Empty-state banner explains the gap
 * so this screen still passes the smoke test pre-wire.
 */
@Composable
fun TokenRefreshLogScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val log = env.tokenRefreshLog

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Token Refresh Log")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Token Refresh Log",
                        style = VLFont.h1,
                        color = VLColor.OnSurface,
                    )
                    Text(
                        text = "Each tap on Token Refresh in Dashboard appends an entry here.",
                        style = VLFont.bodyMd,
                        color = VLColor.OnSurfaceVariant,
                    )
                }

                if (log.isEmpty()) {
                    EmptyBanner()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
                        log.forEach { entry -> EntryCard(entry) }
                    }
                }

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
private fun EmptyBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "No refresh recorded yet. Trigger a refresh from Dashboard → Token Refresh tile.",
                style = VLFont.bodyMd,
                color = VLColor.OnSurface,
            )
        }
    }
}

@Composable
private fun EntryCard(entry: TokenRefreshLogEntry) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = VLColor.Primary,
                )
                Text(
                    text = formatTimestamp(entry.timestampEpochMillis),
                    style = VLFont.labelBold,
                    color = VLColor.OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (entry.expiresAtEpochSeconds > 0L) {
                    VLStatePill(
                        text = "exp ${formatTime(entry.expiresAtEpochSeconds * 1000L)}",
                        tone = VLPillTone.Neutral,
                    )
                }
            }
            HorizontalDivider(color = VLColor.OutlineVariant.copy(alpha = 0.4f))
            VLPropertyRow(
                label = "Old token",
                value = entry.oldTokenPreview,
                icon = Icons.Filled.Lock,
                copyable = true,
            )
            VLPropertyRow(
                label = "New token",
                value = entry.newTokenPreview,
                icon = Icons.Filled.Refresh,
                copyable = true,
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))

private fun formatTime(epochMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun TokenRefreshLogScreenPreview() {
    VLTheme {
        TokenRefreshLogScreen(onBack = {})
    }
}
