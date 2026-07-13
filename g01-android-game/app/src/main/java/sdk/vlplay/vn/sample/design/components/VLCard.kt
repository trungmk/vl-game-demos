package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Surface card with optional title + footnote. Parity iOS [VLCard].
 */
@Composable
fun VLCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    footnote: String? = null,
    padding: PaddingValues = PaddingValues(VLSpacing.md),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.lg),
        colors = CardDefaults.cardColors(containerColor = VLColor.SurfaceContainerLowest),
        border = BorderStroke(0.5.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = VLColor.OnSurface,
                )
            }
            content()
            footnote?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = VLColor.OnSurfaceVariant,
                )
            }
        }
    }
}

/** Section header above a card group. Parity iOS [VLSectionHeader]. */
@Composable
fun VLSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = VLColor.OnSurface,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLCardPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            VLSectionHeader(title = "Account", subtitle = "Identity verified")
            VLCard(title = "Profile") {
                Text(
                    text = "Card body content goes here",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VLCard(footnote = "Auto-saved 2 minutes ago") {
                Text(
                    text = "No title, with footnote",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
