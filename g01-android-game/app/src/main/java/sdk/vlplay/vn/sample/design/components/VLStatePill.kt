package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Status pill — full pill shape, color tied to semantic state.
 * Parity iOS [VLStatePill] / [VLPillTone].
 *
 * Used for Guest/Linked/Verified/Failed/Pending/Most Popular/SUCCESS/STAGING etc.
 */
enum class VLPillTone(
    val background: Color,
    val foreground: Color,
) {
    Neutral(VLColor.SecondaryContainer, VLColor.OnSecondaryContainer),
    Primary(VLColor.PrimaryContainer, VLColor.OnPrimaryContainer),
    Tertiary(VLColor.TertiaryContainer, VLColor.OnTertiaryContainer),
    Success(VLColor.SuccessContainer, Color(0xFF1B5E20)),
    Warning(VLColor.WarningContainer, Color(0xFFB35400)),
    Error(VLColor.ErrorContainer, VLColor.OnErrorContainer),
    Secondary(VLColor.SurfaceContainerHigh, VLColor.OnSurfaceVariant),
    Accent(VLColor.Tertiary, Color.White),
}

@Composable
fun VLStatePill(
    text: String,
    modifier: Modifier = Modifier,
    tone: VLPillTone = VLPillTone.Neutral,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tone.background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let {
            Icon(
                imageVector = it, contentDescription = null,
                tint = tone.foreground, modifier = Modifier,
            )
        }
        Text(
            text = text,
            color = tone.foreground,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLStatePillPreview() {
    VLTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
        ) {
            VLPillTone.values().forEach { tone ->
                VLStatePill(text = tone.name, tone = tone)
            }
        }
    }
}
