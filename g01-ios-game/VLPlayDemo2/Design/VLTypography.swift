import SwiftUI

// Typography stack per mockup spec (DESIGN.md).
// Mockup calls for Roboto; iOS demo uses SF Pro (Font.system) — pixel-close
// for a dev-tool. Bundle Roboto only if Mark requests strict parity.

enum VLFont {
    // Display / Heading
    static let h1            = Font.system(size: 24, weight: .bold)
    static let h2            = Font.system(size: 20, weight: .bold)

    // Body
    static let bodyLg        = Font.system(size: 16, weight: .regular)
    static let bodyMd        = Font.system(size: 14, weight: .regular)
    static let bodySm        = Font.system(size: 12, weight: .regular)

    // Label
    static let labelBold     = Font.system(size: 12, weight: .bold).lowercaseSmallCaps()
    static let labelMd       = Font.system(size: 12, weight: .medium)

    // Code / monospace (debug boxes, IDs, tokens)
    static let code          = Font.system(size: 12, weight: .regular, design: .monospaced)
    static let codeMd        = Font.system(size: 13, weight: .medium, design: .monospaced)

    // Legacy aliases (backward-compat)
    static let title         = h1
    static let sectionTitle  = Font.system(size: 17, weight: .semibold)
    static let rowTitle      = Font.system(size: 15, weight: .medium)
    static let caption       = bodySm
    static let button        = Font.system(size: 14, weight: .semibold)
    static let hero          = Font.system(size: 28, weight: .heavy)
}
