package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Purchase History row tone. Maps to [VLPillTone] for status pill.
 * Parity iOS [VLTransactionStatus].
 */
enum class VLTransactionStatus(val pillText: String, val pillTone: VLPillTone) {
    Success("SUCCESS", VLPillTone.Success),
    Pending("PENDING", VLPillTone.Warning),
    Failed("FAILED", VLPillTone.Error),
}

data class VLTransactionCardData(
    val title: String,
    val amount: String,
    val datetime: String,
    val status: VLTransactionStatus,
    val purchaseCode: String? = null,
    val errorReason: String? = null,
)

/**
 * Purchase History row (d3). Title + amount + datetime + status pill + debug box.
 * Parity iOS [VLTransactionCard].
 */
@Composable
fun VLTransactionCard(
    data: VLTransactionCardData,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = VLColor.OnSurface,
                    )
                    Text(
                        text = data.datetime,
                        style = MaterialTheme.typography.bodySmall,
                        color = VLColor.OnSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = VLSpacing.sm))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = data.amount,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = VLColor.OnSurface,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                    VLStatePill(text = data.status.pillText, tone = data.status.pillTone)
                }
            }

            data.purchaseCode?.let { code ->
                val lines = buildList {
                    add("purchaseCode: $code")
                    data.errorReason?.let { add("error: $it") }
                }
                VLDebugBox(
                    lines = lines,
                    tone = if (data.status == VLTransactionStatus.Failed)
                        VLDebugBoxTone.Error else VLDebugBoxTone.Info,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLTransactionCardPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            VLTransactionCard(
                data = VLTransactionCardData(
                    title = "Starter Pack",
                    amount = "22.000 VND",
                    datetime = "2026-04-27 14:32:18",
                    status = VLTransactionStatus.Success,
                    purchaseCode = "TXN-12345-ABC",
                ),
            )
            VLTransactionCard(
                data = VLTransactionCardData(
                    title = "Premium Pack",
                    amount = "120.000 VND",
                    datetime = "2026-04-27 14:35:01",
                    status = VLTransactionStatus.Failed,
                    purchaseCode = "TXN-67890-DEF",
                    errorReason = "PURCHASE_DECLINED (1042)",
                ),
            )
        }
    }
}
