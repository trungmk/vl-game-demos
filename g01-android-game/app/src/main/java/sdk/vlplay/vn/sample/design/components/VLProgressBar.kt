package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Linear progress bar — capsule track + capsule fill. Parity iOS [VLProgressBar].
 * Used for Playtime in g1 Anti-Addiction.
 *
 * @param progress 0.0 .. 1.0 (clamped)
 */
@Composable
fun VLProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    fillColor: Color = VLColor.Primary,
    trackColor: Color = VLColor.OutlineVariant,
) {
    val clamped = progress.coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val totalWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(CircleShape)
                .background(trackColor),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(totalWidth * clamped)
                .clip(CircleShape)
                .background(fillColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLProgressBarPreview() {
    VLTheme {
        Column(modifier = Modifier.padding(VLSpacing.md)) {
            VLProgressBar(progress = 0f)
            Spacer(modifier = Modifier.height(VLSpacing.sm))
            VLProgressBar(progress = 0.35f)
            Spacer(modifier = Modifier.height(VLSpacing.sm))
            VLProgressBar(progress = 0.75f, height = 12.dp, fillColor = VLColor.Warning)
            Spacer(modifier = Modifier.height(VLSpacing.sm))
            VLProgressBar(progress = 1f, fillColor = VLColor.Error)
        }
    }
}
