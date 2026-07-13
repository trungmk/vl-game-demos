package sdk.vlplay.vn.sample.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Debug tab landing page. Parity iOS demo2 [DebugRootView]. Lists 3 sub-screens:
 * Anti-Addiction status / Token refresh log / Remote SDK config. Embedded in
 * the Debug tab of MainTabScreen — back navigation is handled by the parent
 * tab's pop.
 */
@Composable
fun DebugRootScreen(
    onAntiAddiction: () -> Unit,
    onTokenLog: () -> Unit,
    onRemoteConfig: () -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(VLColor.Surface),
    ) {
        VLTopAppBar(variant = VLTopBarVariant.Debugger, environmentBadge = "STAGING")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            SectionHeader(text = "Compliance")
            DebugListCard {
                DebugRow(
                    icon = Icons.Filled.Shield,
                    title = "Anti-Addiction Status",
                    subtitle = "Playtime · Curfew · Demo controls",
                    onClick = onAntiAddiction,
                )
            }

            SectionHeader(text = "Tokens")
            DebugListCard {
                DebugRow(
                    icon = Icons.Filled.Refresh,
                    title = "Token Refresh Log",
                    subtitle = "Recent token refresh history",
                    onClick = onTokenLog,
                )
            }

            SectionHeader(text = "Remote Config")
            DebugListCard {
                DebugRow(
                    icon = Icons.Filled.Tune,
                    title = "Remote SDK Config",
                    subtitle = "CMS-driven features · payment methods · support",
                    onClick = onRemoteConfig,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = VLFont.labelBold,
        color = VLColor.OnSurfaceVariant,
        modifier = Modifier.padding(horizontal = VLSpacing.xs),
    )
}

@Composable
private fun DebugListCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column { content() }
    }
}

@Composable
private fun DebugRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(VLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VLColor.Primary,
            modifier = Modifier.size(28.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = VLFont.bodyLg.copy(fontWeight = FontWeight.SemiBold),
                color = VLColor.OnSurface,
            )
            Text(
                text = subtitle,
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = VLColor.OnSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DebugRootScreenPreview() {
    VLTheme {
        DebugRootScreen(onAntiAddiction = {}, onTokenLog = {}, onRemoteConfig = {})
    }
}
