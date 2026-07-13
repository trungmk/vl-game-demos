package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Country-code dropdown + phone input + Send OTP button. Parity iOS [VLPhoneInputRow].
 * Used in c5 Device Auth + Identity Verification.
 */
@Composable
fun VLPhoneInputRow(
    countryCode: String,
    phone: String,
    onCountryCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSendOTP: () -> Unit,
    modifier: Modifier = Modifier,
    sendDisabled: Boolean = false,
    codes: List<String> = listOf("+84", "+1", "+44", "+86", "+81", "+82"),
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        // Country code dropdown
        Surface(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(VLRadius.md)),
            color = VLColor.SurfaceContainerLowest,
            border = BorderStroke(1.dp, VLColor.OutlineVariant),
            shape = RoundedCornerShape(VLRadius.md),
            onClick = { menuOpen = true },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = countryCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VLColor.OnSurface,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = VLColor.OnSurfaceVariant,
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    codes.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = {
                                onCountryCodeChange(code)
                                menuOpen = false
                            },
                        )
                    }
                }
            }
        }

        // Phone field
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            placeholder = { Text("Phone number", style = MaterialTheme.typography.bodyMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(VLRadius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VLColor.SurfaceContainerLowest,
                unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                focusedBorderColor = VLColor.Primary,
                unfocusedBorderColor = VLColor.OutlineVariant,
            ),
        )

        // Send OTP button
        VLButton(
            text = "Send OTP",
            onClick = onSendOTP,
            modifier = Modifier.width(110.dp),
            kind = VLButtonKind.Secondary,
            fullWidth = false,
            compact = true,
            enabled = !sendDisabled,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLPhoneInputRowPreview() {
    VLTheme {
        var cc by remember { mutableStateOf("+84") }
        var phone by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(VLSpacing.md)) {
            VLPhoneInputRow(
                countryCode = cc,
                phone = phone,
                onCountryCodeChange = { cc = it },
                onPhoneChange = { phone = it },
                onSendOTP = {},
            )
        }
    }
}
