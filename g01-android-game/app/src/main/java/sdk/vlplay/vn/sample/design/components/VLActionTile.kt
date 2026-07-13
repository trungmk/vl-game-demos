package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Square card tile used in b1 Main Hub grid. Parity iOS [VLActionTile].
 * Icon on top + label bottom, surface-container-lowest background, 1px outline.
 */
@Composable
fun VLActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = VLColor.Primary,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = VLSpacing.sm, vertical = VLSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(VLRadius.md))
                    .background(tint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = VLColor.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.height(32.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLActionTilePreview() {
    VLTheme {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            VLActionTile(icon = Icons.Filled.AccountCircle, label = "Profile", onClick = {}, modifier = Modifier.weight(1f))
            VLActionTile(icon = Icons.Filled.ShoppingCart, label = "Shop", onClick = {}, modifier = Modifier.weight(1f))
            VLActionTile(icon = Icons.Filled.CardGiftcard, label = "Giftcode", onClick = {}, modifier = Modifier.weight(1f), enabled = false)
        }
    }
}
