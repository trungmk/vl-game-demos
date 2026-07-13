import SwiftUI

// Material Symbols → SF Symbols mapping per Q12 decision (use SF Symbols on iOS).
// Source mockup names left-side, SF Symbol equivalents right-side.

enum VLIcon {
    // Brand / debug
    static let developerMode    = "chevron.left.forwardslash.chevron.right"
    static let dashboard        = "square.grid.2x2.fill"
    static let terminal         = "terminal.fill"
    static let bugReport        = "ant.fill"
    static let badge            = "person.crop.circle.fill"

    // Main hub grid actions
    static let profile          = "person.fill"
    static let shop             = "bag.fill"
    static let mail             = "envelope.fill"   // unused per Q1
    static let bind             = "link"
    static let giftcode         = "gift.fill"
    static let otp              = "key.fill"
    static let upgrade          = "arrow.up.circle.fill"
    static let password         = "lock.fill"        // unused per Q1
    static let antiAddiction    = "shield.checkered"
    static let tokenRefresh     = "arrow.triangle.2.circlepath"
    static let identityCCCD     = "person.text.rectangle.fill"

    // Footer / bottom actions
    static let logout           = "rectangle.portrait.and.arrow.right"
    static let delete           = "trash.fill"

    // Tab icons
    static let tabDashboard     = "square.grid.2x2"
    static let tabAuth          = "person.crop.circle"
    static let tabLogs          = "list.bullet.rectangle"
    static let tabDebug         = "terminal"
    static let tabSettings      = "gearshape"

    // Status / feedback
    static let success          = "checkmark.circle.fill"
    static let info             = "info.circle.fill"
    static let warning          = "exclamationmark.triangle.fill"
    static let errorOctagon     = "xmark.octagon.fill"
    static let shieldOk         = "checkmark.shield.fill"
    static let shieldErr        = "exclamationmark.shield.fill"
    static let close            = "xmark"
    static let chevronRight     = "chevron.right"
    static let arrowRight       = "arrow.right"

    // Profile / details
    static let email            = "envelope"
    static let phone            = "phone.fill"
    static let person           = "person"
    static let calendar         = "calendar"
    static let card             = "creditcard"
    static let lock             = "lock"
    static let lockRotate       = "lock.rotation"

    // Anti-addiction
    static let clock            = "clock.fill"
    static let moon             = "moon.fill"
    static let gavel            = "hand.raised.fill"

    // Logs / shop
    static let storefront       = "bag"
    static let history          = "clock.arrow.circlepath"
    static let archive          = "archivebox.fill"
    static let diamond          = "diamond.fill"
    static let gem              = "sparkles"

    // Misc
    static let refresh          = "arrow.clockwise"
    static let settings         = "gearshape.fill"
    static let copy             = "doc.on.doc"
    static let download         = "arrow.down.circle.fill"
    static let tag              = "tag.fill"
    static let photo            = "photo.fill"
}
