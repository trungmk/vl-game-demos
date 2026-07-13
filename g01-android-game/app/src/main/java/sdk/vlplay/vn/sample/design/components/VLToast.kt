package sdk.vlplay.vn.sample.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme

enum class VLToastKind { Info, Success, Error, Warning }

data class VLToastConfig(
    val kind: VLToastKind,
    val title: String,
    val message: String? = null,
)

/**
 * Toast center — observable toast state. Parity iOS [VLToastCenter].
 * Hosts call [show]/[success]/[error]/[info]/[warning]; UI reads [current].
 */
class VLToastCenter {
    private val _current: MutableState<VLToastConfig?> = mutableStateOf(null)
    val current: VLToastConfig? get() = _current.value
    private var token: Int = 0
    val tick: Int get() = token

    fun show(config: VLToastConfig) {
        _current.value = config
        token++
    }

    fun success(title: String, message: String? = null) =
        show(VLToastConfig(VLToastKind.Success, title, message))

    fun error(title: String, message: String? = null) =
        show(VLToastConfig(VLToastKind.Error, title, message))

    fun info(title: String, message: String? = null) =
        show(VLToastConfig(VLToastKind.Info, title, message))

    fun warning(title: String, message: String? = null) =
        show(VLToastConfig(VLToastKind.Warning, title, message))

    fun dismiss() {
        _current.value = null
    }
}

@Composable
fun rememberVLToastCenter(): VLToastCenter = remember { VLToastCenter() }

/**
 * Bottom-anchored toast host. Parity iOS [VLToastOverlay].
 * Auto-dismiss after [durationMillis]; place over content as a Box overlay.
 */
@Composable
fun VLToastHost(
    center: VLToastCenter,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2400,
) {
    val toast = center.current
    val tick = center.tick

    LaunchedEffect(tick) {
        if (toast != null) {
            delay(durationMillis)
            if (center.tick == tick) center.dismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            toast?.let { ToastBanner(it) }
        }
    }
}

@Composable
private fun ToastBanner(toast: VLToastConfig) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VLSpacing.md, vertical = VLSpacing.lg),
        shape = RoundedCornerShape(14.dp),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(0.5.dp, VLColor.OutlineVariant),
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = iconFor(toast.kind),
                contentDescription = null,
                tint = tintFor(toast.kind),
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = toast.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = VLColor.OnSurface,
                )
                toast.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = VLColor.OnSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.size(0.dp))
        }
    }
}

private fun iconFor(kind: VLToastKind): ImageVector = when (kind) {
    VLToastKind.Info -> Icons.Filled.Info
    VLToastKind.Success -> Icons.Filled.CheckCircle
    VLToastKind.Error -> Icons.Filled.Error
    VLToastKind.Warning -> Icons.Filled.Warning
}

private fun tintFor(kind: VLToastKind): Color = when (kind) {
    VLToastKind.Info -> VLColor.Tertiary
    VLToastKind.Success -> VLColor.Success
    VLToastKind.Error -> VLColor.Error
    VLToastKind.Warning -> VLColor.Warning
}

@Preview(showBackground = true)
@Composable
private fun VLToastPreview() {
    VLTheme {
        val center = rememberVLToastCenter()
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(VLSpacing.md), verticalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
                VLButton(text = "Show success", onClick = { center.success("Saved", "Profile updated") })
                VLButton(text = "Show error", onClick = { center.error("Network error", "Could not reach server") }, kind = VLButtonKind.Destructive)
            }
            VLToastHost(center = center)
        }
    }
}
