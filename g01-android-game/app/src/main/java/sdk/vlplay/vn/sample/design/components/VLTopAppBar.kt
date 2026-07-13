package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Top app bar — 2 variants per mockup. Parity iOS [VLTopAppBar].
 *
 * - [Debugger] "SDK_DEBUGGER" + developer-mode icon (root tab screens, gray tone)
 * - [Brand] "VLPlay SDK" + red diamond logo (detail screens)
 */
enum class VLTopBarVariant { Debugger, Brand }

@Composable
fun VLTopAppBar(
    modifier: Modifier = Modifier,
    variant: VLTopBarVariant = VLTopBarVariant.Debugger,
    subtitle: String? = null,
    environmentBadge: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    // Layout parity iOS VLTopAppBar.swift: logo · title/subtitle · env-pill · Spacer · trailing.
    // The Column wraps its content (no weight); a trailing Spacer pushes `trailing()` to the
    // far right so the env-pill sits adjacent to the title instead of stretched apart.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VLColor.Surface)
            .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        LogoMark(variant = variant)
        Column {
            Text(
                text = if (variant == VLTopBarVariant.Debugger) "SDK_DEBUGGER" else "VLPlay SDK",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (variant == VLTopBarVariant.Debugger) VLColor.OnSurfaceVariant else VLColor.Primary,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = VLColor.OnSurfaceVariant,
                )
            }
        }
        environmentBadge?.let {
            VLStatePill(
                text = it,
                tone = if (variant == VLTopBarVariant.Debugger) VLPillTone.Neutral else VLPillTone.Primary,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun LogoMark(variant: VLTopBarVariant) {
    when (variant) {
        VLTopBarVariant.Debugger -> Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(VLRadius.sm))
                .background(VLColor.SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.DeveloperMode,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
        VLTopBarVariant.Brand -> Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(VLRadius.sm))
                    .background(VLColor.Primary),
            )
            Text(
                text = "VL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                ),
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLTopAppBarDebuggerPreview() {
    VLTheme {
        VLTopAppBar(
            variant = VLTopBarVariant.Debugger,
            subtitle = "Phase 0 ready",
            environmentBadge = "STAGING",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLTopAppBarBrandPreview() {
    VLTheme {
        VLTopAppBar(
            variant = VLTopBarVariant.Brand,
            subtitle = "Profile",
            environmentBadge = "PROD",
        )
    }
}
