import Foundation
import Combine

// Read-only bridge to AntiAddictionManager.shared (server-driven, Decree 147/2024).
//
// SDK owns the heartbeat + status poll + warn/kick popups + auto-signOut.
// This service only mirrors the manager's lastStatus into @Published values so
// SwiftUI can render a live banner / detail screen.
//
// Server returns minute-precision values; status polls every warningIntervalMinutes
// (default 30). Between polls we tick a local 1-second timer that interpolates
// `remainingSessionSeconds` so the countdown UI feels alive. On each new status
// update from the server we resync the local clock.

final class AntiAddictionTimerService: ObservableObject {

    @Published private(set) var hasStatus: Bool = false
    @Published private(set) var remainingSessionSeconds: Int = 0
    @Published private(set) var remainingTodayMinutes: Int = 0
    @Published private(set) var currentSessionMinutes: Int = 0
    @Published private(set) var totalPlayedTodayMinutes: Int = 0
    @Published private(set) var ageGroup: String = ""
    @Published private(set) var curfewActive: Bool = false
    @Published private(set) var nextCurfewStart: String? = nil
    @Published private(set) var shouldWarn: Bool = false
    @Published private(set) var shouldKick: Bool = false
    @Published private(set) var lastSyncAt: Date? = nil

    private var ticker: Timer?
    private var lastSeenStatus: AntiAddictionStatus?

    /// Manager exposes its current config; surface for debug screens.
    var config: AntiAddictionConfig? { AntiAddictionManager.shared().config }
    /// Whether SDK has an active session (heartbeat + status polling running).
    var sessionActive: Bool { AntiAddictionManager.shared().sessionActive }

    init() {
        startTicking()
        sync()
    }

    deinit {
        ticker?.invalidate()
    }

    /// Force a status fetch now (no-op if session inactive).
    func refreshNow() {
        AntiAddictionManager.shared().checkStatusNow()
    }

    /// HH:mm:ss for the current session countdown.
    var formattedRemainingSession: String {
        let s = max(0, remainingSessionSeconds)
        return String(format: "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
    }

    /// "2h 35m" / "45m" — for "remaining today" badge.
    var formattedRemainingToday: String {
        let m = max(0, remainingTodayMinutes)
        if m >= 60 {
            return "\(m / 60)h \(m % 60)m"
        }
        return "\(m)m"
    }

    var ageGroupLabel: String {
        switch ageGroup {
        case "underage": return "Dưới 16 tuổi"
        case "teen":     return "16–17 tuổi"
        case "adult":    return "Từ 18 tuổi"
        default:         return ageGroup.isEmpty ? "—" : ageGroup
        }
    }

    // MARK: - Internal

    private func startTicking() {
        ticker?.invalidate()
        ticker = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            self?.tick()
        }
    }

    /// Pull the manager's lastStatus and republish if it changed.
    private func sync() {
        guard let status = AntiAddictionManager.shared().lastStatus else { return }
        applyStatus(status, isNew: status !== lastSeenStatus)
    }

    private func tick() {
        let manager = AntiAddictionManager.shared()
        if let status = manager.lastStatus, status !== lastSeenStatus {
            applyStatus(status, isNew: true)
            return
        }
        // Local interpolation between server polls.
        guard hasStatus, let last = lastSyncAt, let status = lastSeenStatus else { return }
        let elapsed = Int(Date().timeIntervalSince(last))
        let baseSeconds = Self.countdownBaseSeconds(for: status)
        remainingSessionSeconds = max(0, baseSeconds - elapsed)
    }

    private func applyStatus(_ status: AntiAddictionStatus, isNew: Bool) {
        lastSeenStatus = status
        hasStatus = true
        ageGroup = status.ageGroup
        totalPlayedTodayMinutes = status.totalPlayedTodayMinutes
        remainingTodayMinutes = status.remainingMinutesToday
        currentSessionMinutes = status.currentSessionMinutes
        remainingSessionSeconds = Self.countdownBaseSeconds(for: status)
        curfewActive = status.curfewActive
        nextCurfewStart = status.nextCurfewStart
        shouldWarn = status.shouldWarn
        shouldKick = status.shouldKick
        if isNew { lastSyncAt = Date() }
    }

    /// Pick the countdown source. Per-session limit (`remainingSessionMinutes`)
    /// only applies to underage/teen ageGroups — `ageLimits` config has
    /// `*MaxMinutesPerSession` for those tiers but adults only get
    /// `adultMaxMinutesPerDay`. When BE returns `remainingSessionMinutes = 0`
    /// (no session cap), fall back to daily remaining so the ring isn't blank.
    /// Parity Android `applyStatus` line 217.
    private static func countdownBaseSeconds(for status: AntiAddictionStatus) -> Int {
        if status.remainingSessionMinutes > 0 {
            return status.remainingSessionMinutes * 60
        }
        return max(0, status.remainingMinutesToday) * 60
    }
}
