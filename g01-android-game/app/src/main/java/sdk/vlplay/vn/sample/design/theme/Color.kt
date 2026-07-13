package sdk.vlplay.vn.sample.design.theme

import androidx.compose.ui.graphics.Color

/**
 * VLPlay color tokens — parity iOS demo2 [VLTheme.swift].
 * Material 3 design tokens, "Corporate / Modern / Utility-First / glass-box".
 */
object VLColor {
    // Brand & primary — synced với LANDSCAPE-REDESIGN single brand red #FF4D4D (2026-05-07).
    val Primary = Color(0xFFFF4D4D)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFFFDAD7)
    val OnPrimaryContainer = Color(0xFF410006)

    // Surface / background
    val Surface = Color(0xFFFFF8F7)
    val SurfaceDim = Color(0xFFE8D6D4)
    val SurfaceContainerLowest = Color.White
    val SurfaceContainerLow = Color(0xFFFFF0EE)
    val SurfaceContainer = Color(0xFFFCE8E5)
    val SurfaceContainerHigh = Color(0xFFF6E2DF)
    val SurfaceContainerHighest = Color(0xFFF0DCD9)
    val OnSurface = Color(0xFF231918)
    val OnSurfaceVariant = Color(0xFF534342)

    // Tertiary (teal — guest badge, status)
    val Tertiary = Color(0xFF006581)
    val OnTertiary = Color.White
    val TertiaryContainer = Color(0xFFB9EAFF)
    val OnTertiaryContainer = Color(0xFF001F2A)

    // Error
    val Error = Color(0xFFBA1A1A)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // Outline
    val Outline = Color(0xFF926E6C)
    val OutlineVariant = Color(0xFFE7BCB9)

    // Secondary
    val Secondary = Color(0xFF775653)
    val SecondaryContainer = Color(0xFFE2E2E5)
    val OnSecondaryContainer = Color(0xFF2C1513)

    // Status semantic (logs/transactions/anti-addiction)
    val Success = Color(0xFF2E7D32)
    val SuccessContainer = Color(0xFFC8E6C9)
    val Warning = Color(0xFFED6C02)
    val WarningContainer = Color(0xFFFFE0B2)

    // Legacy aliases (parity iOS — backward-compat with views during migration)
    val PrimaryDark = Color(0xFFCC2626) // hover/pressed brand red
    val Hero = Color(0xFF1A1A2E)
}
