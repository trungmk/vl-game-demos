package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Pink/error-tinted box with monospace content. Parity iOS [VLDebugBox].
 * Used for: purchaseCode in d3 PurchaseHistory, Affected Resources in c6
 * DeleteAccount, demo control output in g1 Debug.
 */
enum class VLDebugBoxTone(
    val background: Color,
    val outline: Color,
    val titleColor: Color,
    val bodyColor: Color,
) {
    /** Faint pink — neutral debug payload */
    Info(
        background = VLColor.ErrorContainer.copy(alpha = 0.45f),
        outline = VLColor.OutlineVariant,
        titleColor = VLColor.OnErrorContainer,
        bodyColor = VLColor.OnErrorContainer,
    ),

    /** Strong pink — error payload */
    Error(
        background = VLColor.ErrorContainer,
        outline = VLColor.Error.copy(alpha = 0.4f),
        titleColor = VLColor.Error,
        bodyColor = VLColor.OnErrorContainer,
    ),

    /** Green — success payload */
    Success(
        background = VLColor.SuccessContainer,
        outline = Color(0xFF4CAF50).copy(alpha = 0.4f),
        titleColor = Color(0xFF1B5E20),
        bodyColor = Color(0xFF1B5E20),
    ),
}

@Composable
fun VLDebugBox(
    lines: List<String>,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: VLDebugBoxTone = VLDebugBoxTone.Info,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = tone.background,
        border = BorderStroke(1.dp, tone.outline),
    ) {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    ),
                    color = tone.titleColor,
                )
            }
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                    ),
                    color = tone.bodyColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLDebugBoxPreview() {
    VLTheme {
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            VLDebugBox(
                title = "TRANSACTION",
                lines = listOf(
                    "purchaseCode: TXN-12345-ABC",
                    "amount: 22000 VND",
                    "timestamp: 2026-04-27T14:32:18Z",
                ),
                tone = VLDebugBoxTone.Info,
            )
            VLDebugBox(
                title = "ERROR 1042",
                lines = listOf(
                    "PURCHASE_DECLINED",
                    "code: 1042",
                    "message: Card refused by issuer",
                ),
                tone = VLDebugBoxTone.Error,
            )
            VLDebugBox(
                title = "OK",
                lines = listOf("OTP sent to +84901234567", "expiresIn: 60s"),
                tone = VLDebugBoxTone.Success,
            )
        }
    }
}
