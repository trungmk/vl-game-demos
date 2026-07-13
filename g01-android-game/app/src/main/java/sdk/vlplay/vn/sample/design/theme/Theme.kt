package sdk.vlplay.vn.sample.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light color scheme — parity iOS demo2 (no dark mode in iOS demo2 yet).
 */
private val VLLightColorScheme = lightColorScheme(
    primary = VLColor.Primary,
    onPrimary = VLColor.OnPrimary,
    primaryContainer = VLColor.PrimaryContainer,
    onPrimaryContainer = VLColor.OnPrimaryContainer,

    secondary = VLColor.Secondary,
    secondaryContainer = VLColor.SecondaryContainer,
    onSecondaryContainer = VLColor.OnSecondaryContainer,

    tertiary = VLColor.Tertiary,
    onTertiary = VLColor.OnTertiary,
    tertiaryContainer = VLColor.TertiaryContainer,
    onTertiaryContainer = VLColor.OnTertiaryContainer,

    error = VLColor.Error,
    onError = VLColor.OnError,
    errorContainer = VLColor.ErrorContainer,
    onErrorContainer = VLColor.OnErrorContainer,

    surface = VLColor.Surface,
    onSurface = VLColor.OnSurface,
    surfaceVariant = VLColor.SurfaceContainerLow,
    onSurfaceVariant = VLColor.OnSurfaceVariant,
    surfaceContainerLowest = VLColor.SurfaceContainerLowest,
    surfaceContainerLow = VLColor.SurfaceContainerLow,
    surfaceContainer = VLColor.SurfaceContainer,
    surfaceContainerHigh = VLColor.SurfaceContainerHigh,
    surfaceContainerHighest = VLColor.SurfaceContainerHighest,

    outline = VLColor.Outline,
    outlineVariant = VLColor.OutlineVariant,
)

@Composable
fun VLTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VLLightColorScheme,
        typography = VLTypography,
        content = content,
    )
}
