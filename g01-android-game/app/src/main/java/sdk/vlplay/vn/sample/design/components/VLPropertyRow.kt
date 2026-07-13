package sdk.vlplay.vn.sample.design.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Key-value split row. Parity iOS [VLPropertyRow]. Used in Profile,
 * RemoteConfig views.
 */
@Composable
fun VLPropertyRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    copyable: Boolean = false,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.padding(vertical = VLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        if (icon != null) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = VLColor.OnSurfaceVariant)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = VLColor.OnSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = VLColor.OnSurface,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(VLSpacing.sm))
        if (copyable) {
            IconButton(onClick = { copyToClipboard(context, label, value) }) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    tint = VLColor.Primary,
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
}

@Preview(showBackground = true)
@Composable
private fun VLPropertyRowPreview() {
    VLTheme {
        Column(modifier = Modifier.padding(VLSpacing.md)) {
            VLPropertyRow(label = "User ID", value = "65a3f9c2e5f7b8d9c0a1b2c3", copyable = true)
            VLPropertyRow(label = "Email", value = "user@vlplay.vn")
            VLPropertyRow(
                label = "Phone",
                value = "0901234567",
                icon = Icons.Filled.ContentCopy,
            )
        }
    }
}
