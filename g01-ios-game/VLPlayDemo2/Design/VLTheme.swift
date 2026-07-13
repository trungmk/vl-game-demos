import SwiftUI

// Material 3 design tokens for VL Demo SDK Test App.
// Mirrors my-notes/demo_app_design/vlplay_core_system/DESIGN.md.
// Brand voice: "Corporate / Modern / Utility-First / glass-box" — dev-tool feel.

enum VLColor {
    // Brand & primary — synced với LANDSCAPE-REDESIGN single brand red #FF4D4D (2026-05-07).
    static let primary             = Color(hex: 0xFF4D4D)
    static let onPrimary           = Color.white
    static let primaryContainer    = Color(hex: 0xFFDAD7)
    static let onPrimaryContainer  = Color(hex: 0x410006)

    // Surface / background
    static let surface             = Color(hex: 0xFFF8F7)
    static let surfaceDim          = Color(hex: 0xE8D6D4)
    static let surfaceContainerLowest = Color.white
    static let surfaceContainerLow = Color(hex: 0xFFF0EE)
    static let surfaceContainer    = Color(hex: 0xFCE8E5)
    static let surfaceContainerHigh = Color(hex: 0xF6E2DF)
    static let surfaceContainerHighest = Color(hex: 0xF0DCD9)
    static let onSurface           = Color(hex: 0x231918)
    static let onSurfaceVariant    = Color(hex: 0x534342)

    // Tertiary (teal — guest badge, status)
    static let tertiary            = Color(hex: 0x006581)
    static let onTertiary          = Color.white
    static let tertiaryContainer   = Color(hex: 0xB9EAFF)
    static let onTertiaryContainer = Color(hex: 0x001F2A)

    // Error
    static let error               = Color(hex: 0xBA1A1A)
    static let onError             = Color.white
    static let errorContainer      = Color(hex: 0xFFDAD6)
    static let onErrorContainer    = Color(hex: 0x410002)

    // Outline
    static let outline             = Color(hex: 0x926E6C)
    static let outlineVariant      = Color(hex: 0xE7BCB9)

    // Secondary container (gray pills, neutral CTA)
    static let secondary           = Color(hex: 0x775653)
    static let secondaryContainer  = Color(hex: 0xE2E2E5)
    static let onSecondaryContainer = Color(hex: 0x2C1513)

    // Status semantic (logs/transactions/anti-addiction)
    static let success             = Color(hex: 0x2E7D32)
    static let successContainer    = Color(hex: 0xC8E6C9)
    static let warning             = Color(hex: 0xED6C02)
    static let warningContainer    = Color(hex: 0xFFE0B2)

    // Legacy aliases (backward-compat with views during migration; remove after Step 9)
    static let primaryDark         = Color(hex: 0xCC2626) // hover/pressed brand red
    static let accent              = tertiary
    static let danger              = error
    static let cardBackground      = surfaceContainerLowest
    static let mutedText           = onSurfaceVariant
    static let border              = outlineVariant
    static let hero                = Color(hex: 0x1A1A2E)
}

// MARK: - Spacing (8px linear scale per design system)
enum VLSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let safeMargin: CGFloat = 20
}

// MARK: - Corner radius
enum VLRadius {
    static let sm: CGFloat = 4
    static let md: CGFloat = 8        // default
    static let lg: CGFloat = 16
    static let pill: CGFloat = 999
}

// MARK: - Elevation
enum VLElevation {
    // 1px outline + soft ambient (only for Home icon container per spec)
    static let ambient = Color.black.opacity(0.08)
    static let ambientRadius: CGFloat = 12
    static let ambientYOffset: CGFloat = 4
}

// MARK: - Legacy metric aliases (backward-compat)
enum VLMetric {
    static let cardCorner:  CGFloat = VLRadius.lg
    static let chipCorner:  CGFloat = VLRadius.pill
    static let cardPadding: CGFloat = VLSpacing.md
    static let spacing:     CGFloat = 12
}

// MARK: - Color helper
extension Color {
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >>  8) & 0xFF) / 255.0
        let b = Double( hex        & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: alpha)
    }
}
