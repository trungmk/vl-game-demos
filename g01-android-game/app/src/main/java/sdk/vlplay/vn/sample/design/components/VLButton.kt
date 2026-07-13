package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Button style kinds — parity iOS [VLButtonStyle] / [VLButtonStyleKind].
 *
 * - [Primary] filled red CTA — main action
 * - [Secondary] outlined red on white — secondary CTA
 * - [Destructive] filled red — Delete / Force Kick
 * - [Ghost] text only — Cancel / textual link
 * - [Neutral] filled gray — "Chơi Ngay (Guest)"
 */
enum class VLButtonKind { Primary, Secondary, Destructive, Ghost, Neutral }

@Composable
fun VLButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: VLButtonKind = VLButtonKind.Primary,
    fullWidth: Boolean = true,
    compact: Boolean = false,
    enabled: Boolean = true,
) {
    val widthMod = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val heightDp = if (compact) 36.dp else 48.dp
    val horizontalPadding = if (compact) 14.dp else 20.dp
    val padding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp)
    val mod = modifier.then(widthMod).heightIn(min = heightDp)

    when (kind) {
        VLButtonKind.Primary -> Button(
            onClick = onClick, modifier = mod, enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = VLColor.Primary,
                contentColor = VLColor.OnPrimary,
            ),
            contentPadding = padding,
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }

        VLButtonKind.Secondary -> OutlinedButton(
            onClick = onClick, modifier = mod, enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = VLColor.SurfaceContainerLowest,
                contentColor = VLColor.Primary,
            ),
            border = BorderStroke(1.2.dp, VLColor.Primary),
            contentPadding = padding,
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }

        VLButtonKind.Destructive -> Button(
            onClick = onClick, modifier = mod, enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = VLColor.Error,
                contentColor = VLColor.OnError,
            ),
            contentPadding = padding,
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }

        VLButtonKind.Ghost -> TextButton(
            onClick = onClick, modifier = mod, enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.textButtonColors(contentColor = VLColor.OnSurfaceVariant),
            border = BorderStroke(1.dp, VLColor.OutlineVariant),
            contentPadding = padding,
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }

        VLButtonKind.Neutral -> Button(
            onClick = onClick, modifier = mod, enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = VLColor.SecondaryContainer,
                contentColor = VLColor.OnSecondaryContainer,
            ),
            contentPadding = padding,
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLButtonPreview() {
    VLTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(VLSpacing.sm),
        ) {
            VLButton(text = "Primary CTA", onClick = {}, kind = VLButtonKind.Primary)
            VLButton(text = "Secondary", onClick = {}, kind = VLButtonKind.Secondary)
            VLButton(text = "Destructive", onClick = {}, kind = VLButtonKind.Destructive)
            VLButton(text = "Ghost", onClick = {}, kind = VLButtonKind.Ghost)
            VLButton(text = "Neutral / Guest", onClick = {}, kind = VLButtonKind.Neutral)
            VLButton(text = "Compact", onClick = {}, kind = VLButtonKind.Primary, compact = true, fullWidth = false)
        }
    }
}
