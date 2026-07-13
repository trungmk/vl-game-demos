package sdk.vlplay.vn.sample.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import sdk.vlplay.vn.sample.design.theme.VLColor

/**
 * Generic API call lifecycle state. Parity iOS [APICallResult].
 * Used by demo screens to render a status badge alongside a "Test API" button.
 */
sealed class APICallResult {
    object Idle : APICallResult()
    object Loading : APICallResult()
    data class Success(val message: String) : APICallResult()
    data class Failure(val message: String) : APICallResult()

    val color: Color get() = when (this) {
        is Idle -> VLColor.OnSurfaceVariant
        is Loading -> VLColor.Warning
        is Success -> VLColor.Success
        is Failure -> VLColor.Error
    }

    val text: String get() = when (this) {
        is Idle -> "idle"
        is Loading -> "loading…"
        is Success -> "✓ $message"
        is Failure -> "✗ $message"
    }
}

/** Compact text badge — parity iOS [ResultBadge]. */
@Composable
fun ResultBadge(result: APICallResult, modifier: Modifier = Modifier) {
    Text(
        text = result.text,
        style = MaterialTheme.typography.bodySmall,
        color = result.color,
        modifier = modifier,
    )
}
