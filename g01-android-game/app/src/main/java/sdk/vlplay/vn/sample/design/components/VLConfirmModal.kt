package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

enum class VLConfirmModalVariant { Info, Destructive }

data class VLConfirmModalContent(
    val title: String,
    val message: String,
    val confirmTitle: String,
    val cancelTitle: String,
    val variant: VLConfirmModalVariant,
)

/**
 * Centered confirm dialog. Parity iOS [VLConfirmModal].
 * `Destructive` variant shows red triangle warning + red Confirm button.
 */
@Composable
fun VLConfirmModal(
    content: VLConfirmModalContent,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(VLSpacing.lg),
            shape = RoundedCornerShape(VLRadius.lg),
            color = VLColor.SurfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(VLSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                if (content.variant == VLConfirmModalVariant.Destructive) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = VLColor.Error,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = VLColor.OnSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = content.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VLColor.OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
                ) {
                    VLButton(
                        text = content.cancelTitle,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        kind = VLButtonKind.Secondary,
                    )
                    VLButton(
                        text = content.confirmTitle,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        kind = if (content.variant == VLConfirmModalVariant.Destructive)
                            VLButtonKind.Destructive else VLButtonKind.Primary,
                    )
                }
            }
        }
    }
}

/**
 * Centralized confirm pending state. Parity iOS [VLConfirmCenter].
 * Use [ask] from a screen, then host a [VLConfirmHost] in your scaffold.
 */
class VLConfirmCenter {
    private val _pending: MutableState<VLConfirmModalContent?> = mutableStateOf(null)
    val pending: VLConfirmModalContent? get() = _pending.value
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun ask(
        content: VLConfirmModalContent,
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {},
    ) {
        _pending.value = content
        this.onConfirm = onConfirm
        this.onCancel = onCancel
    }

    fun confirm() {
        val cb = onConfirm
        clear()
        cb?.invoke()
    }

    fun cancel() {
        val cb = onCancel
        clear()
        cb?.invoke()
    }

    private fun clear() {
        _pending.value = null
        onConfirm = null
        onCancel = null
    }
}

@Composable
fun rememberVLConfirmCenter(): VLConfirmCenter = remember { VLConfirmCenter() }

@Composable
fun VLConfirmHost(center: VLConfirmCenter) {
    center.pending?.let { content ->
        VLConfirmModal(
            content = content,
            onConfirm = { center.confirm() },
            onCancel = { center.cancel() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VLConfirmModalPreview() {
    VLTheme {
        VLConfirmModal(
            content = VLConfirmModalContent(
                title = "Xóa tài khoản?",
                message = "Hành động này không thể hoàn tác.",
                confirmTitle = "Xóa",
                cancelTitle = "Hủy",
                variant = VLConfirmModalVariant.Destructive,
            ),
            onConfirm = {},
            onCancel = {},
        )
    }
}
