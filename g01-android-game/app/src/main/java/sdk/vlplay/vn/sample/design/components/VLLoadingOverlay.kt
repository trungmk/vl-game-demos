package sdk.vlplay.vn.sample.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

/**
 * Loading center — observable spinner state. Parity iOS [VLLoadingCenter].
 * Use [show]/[hide] from a screen; host a [VLLoadingOverlay] in your scaffold.
 */
class VLLoadingCenter {
    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)
    private val _message: MutableState<String?> = mutableStateOf(null)
    val isLoading: Boolean get() = _isLoading.value
    val message: String? get() = _message.value

    fun show(message: String? = null) {
        _message.value = message
        _isLoading.value = true
    }

    fun hide() {
        _isLoading.value = false
        _message.value = null
    }
}

@Composable
fun rememberVLLoadingCenter(): VLLoadingCenter = remember { VLLoadingCenter() }

/**
 * Full-screen dimmed overlay with centered spinner + optional message.
 * Parity iOS [VLLoadingOverlay]. Place as the topmost layer in your Box scaffold.
 */
@Composable
fun VLLoadingOverlay(
    center: VLLoadingCenter,
    modifier: Modifier = Modifier,
) {
    if (!center.isLoading) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(VLRadius.lg),
            color = VLColor.SurfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(VLSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = VLColor.Primary,
                    strokeWidth = 3.dp,
                )
                center.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VLColor.OnSurface,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VLLoadingOverlayPreview() {
    VLTheme {
        val center = rememberVLLoadingCenter()
        center.show("Đang đăng nhập…")
        Box(modifier = Modifier.fillMaxSize()) {
            VLLoadingOverlay(center = center)
        }
    }
}
