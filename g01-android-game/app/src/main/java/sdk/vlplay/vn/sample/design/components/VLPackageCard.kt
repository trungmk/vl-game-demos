package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Plain data carrier for [VLPackageCard]. Parity iOS [VLPackageCardData].
 * `productId` is the Play Store / App Store product ID passed to billing API.
 */
data class VLPackageCardData(
    val id: String,
    val productId: String,
    val title: String,
    val rewardsHeadline: String,
    val priceLabel: String,
    val mostPopular: Boolean,
) {
    companion object {
        fun formatPriceVND(amount: Long, currency: String): String {
            val nf = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${nf.format(amount)} $currency"
        }
    }
}

/**
 * Shop package card (d1). Parity iOS [VLPackageCard].
 * `mostPopular = true` shows badge ribbon + filled primary Buy button.
 */
@Composable
fun VLPackageCard(
    data: VLPackageCardData,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(
            if (data.mostPopular) 1.5.dp else 1.dp,
            if (data.mostPopular) VLColor.Primary else VLColor.OutlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            // Header: gem icon + most-popular pill
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(VLRadius.md))
                        .background(VLColor.PrimaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Diamond,
                        contentDescription = null,
                        tint = VLColor.Primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (data.mostPopular) {
                    VLStatePill(text = "MOST POPULAR", tone = VLPillTone.Primary)
                }
            }

            // Title
            Text(
                text = data.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = VLColor.OnSurface,
            )

            // Rewards headline
            if (data.rewardsHeadline.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Diamond,
                        contentDescription = null,
                        tint = VLColor.Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = data.rewardsHeadline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VLColor.OnSurface,
                    )
                }
            }

            // Price
            Text(
                text = data.priceLabel,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = VLColor.OnSurface,
            )

            // Buy button
            VLButton(
                text = if (data.mostPopular) "Buy Premium" else "Buy",
                onClick = onBuy,
                kind = if (data.mostPopular) VLButtonKind.Primary else VLButtonKind.Secondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLPackageCardPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            VLPackageCard(
                data = VLPackageCardData(
                    id = "pkg_001",
                    productId = "vn.vlplay.demo.pack_001",
                    title = "Starter Pack",
                    rewardsHeadline = "100 Gems",
                    priceLabel = "22.000 VND",
                    mostPopular = false,
                ),
                onBuy = {},
            )
            VLPackageCard(
                data = VLPackageCardData(
                    id = "pkg_002",
                    productId = "vn.vlplay.demo.pack_002",
                    title = "Premium Pack",
                    rewardsHeadline = "500 Gems + Crate",
                    priceLabel = "120.000 VND",
                    mostPopular = true,
                ),
                onBuy = {},
            )
        }
    }
}
