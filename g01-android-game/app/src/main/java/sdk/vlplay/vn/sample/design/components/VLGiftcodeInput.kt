package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * 16-character giftcode input with auto-format `XXXX-XXXX-XXXX-XXXX`.
 * Parity iOS [VLGiftcodeInput]. Used in f1 Giftcode Redeem.
 *
 * Caller stores [rawCode] (uppercase alphanumerics, no separator, max [maxLength]).
 * Display value is the formatted version.
 */
@Composable
fun VLGiftcodeInput(
    rawCode: String,
    onRawCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 16,
    placeholder: String = "XXXX-XXXX-XXXX-XXXX",
) {
    OutlinedTextField(
        value = formatGiftcode(rawCode),
        onValueChange = { input ->
            val cleaned = input
                .replace("-", "")
                .uppercase()
                .filter { it.isLetterOrDigit() }
                .take(maxLength)
            onRawCodeChange(cleaned)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = VLColor.OnSurface,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false,
        ),
        singleLine = true,
        shape = RoundedCornerShape(VLRadius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VLColor.SurfaceContainerLowest,
            unfocusedContainerColor = VLColor.SurfaceContainerLowest,
            focusedBorderColor = VLColor.Primary,
            unfocusedBorderColor = VLColor.Primary,
        ),
    )
}

private fun formatGiftcode(raw: String): String {
    val sb = StringBuilder()
    raw.forEachIndexed { idx, ch ->
        if (idx > 0 && idx % 4 == 0) sb.append('-')
        sb.append(ch)
    }
    return sb.toString()
}

@Preview(showBackground = true)
@Composable
private fun VLGiftcodeInputPreview() {
    VLTheme {
        var raw by remember { mutableStateOf("ABCD1234WXYZ") }
        Column(modifier = Modifier.padding(VLSpacing.md)) {
            VLGiftcodeInput(rawCode = raw, onRawCodeChange = { raw = it })
        }
    }
}
