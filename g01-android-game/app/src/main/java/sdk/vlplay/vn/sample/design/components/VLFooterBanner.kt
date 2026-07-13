package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
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
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Pink/info-tinted banner used at bottom of cards (c4 "Credentials encrypted",
 * c1 "Read-only view"). Icon + body text. Parity iOS [VLFooterBanner].
 * Tone reuses [VLDebugBoxTone] for visual consistency.
 */
@Composable
fun VLFooterBanner(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    tone: VLDebugBoxTone = VLDebugBoxTone.Info,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = tone.background,
        border = BorderStroke(1.dp, tone.outline),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tone.titleColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = tone.bodyColor,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLFooterBannerPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            VLFooterBanner(
                icon = Icons.Filled.Lock,
                text = "Credentials encrypted with EncryptedSharedPreferences",
                tone = VLDebugBoxTone.Info,
            )
            VLFooterBanner(
                icon = Icons.Filled.WarningAmber,
                text = "This action will permanently delete your account",
                tone = VLDebugBoxTone.Error,
            )
            VLFooterBanner(
                icon = Icons.Filled.Info,
                text = "OTP sent — valid for 60 seconds",
                tone = VLDebugBoxTone.Success,
            )
        }
    }
}
