package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * 6-digit OTP grid with active border. Parity iOS [VLOTPInput].
 * Backed by a hidden BasicTextField that captures input; tap surface to refocus.
 */
@Composable
fun VLOTPInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
            for (i in 0 until length) {
                val char = code.getOrNull(i)?.toString() ?: ""
                val isActive = i == code.length
                val isFilled = char.isNotEmpty()
                val borderColor = if (isActive || isFilled) VLColor.Primary else VLColor.OutlineVariant
                val borderWidth = if (isActive) 2.dp else 1.dp

                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 52.dp)
                        .clip(RoundedCornerShape(VLRadius.md))
                        .background(VLColor.SurfaceContainerLowest)
                        .border(borderWidth, borderColor, RoundedCornerShape(VLRadius.md)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = VLColor.OnSurface,
                    )
                }
            }
        }

        // Hidden text field captures all input
        BasicTextField(
            value = code,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }
                onCodeChange(digits.take(length))
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            singleLine = true,
        )

        // Tap layer to refocus when user taps any digit cell
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(VLRadius.md))
                .background(Color.Transparent),
        )
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
}

/** Countdown chip — capsule with clock icon + `mm:ss`. Parity iOS [VLCountdownChip]. */
@Composable
fun VLCountdownChip(
    secondsRemaining: Int,
    modifier: Modifier = Modifier,
) {
    val mins = secondsRemaining / 60
    val secs = secondsRemaining % 60
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(VLColor.SurfaceContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = VLColor.OnSurfaceVariant,
            modifier = Modifier.size(11.dp),
        )
        Text(
            text = String.format("%02d:%02d", mins, secs),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLOTPInputPreview() {
    VLTheme {
        var code by remember { mutableStateOf("123") }
        Column(
            modifier = Modifier.padding(VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            VLOTPInput(code = code, onCodeChange = { code = it })
            VLCountdownChip(secondsRemaining = 87)
        }
    }
}
