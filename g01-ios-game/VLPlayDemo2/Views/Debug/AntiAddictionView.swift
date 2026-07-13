import SwiftUI
import VLPlaySDK

// g1 — Anti-Addiction status (server-driven, Decree 147/2024).
// Reads from env.antiAddictionTimer which mirrors AntiAddictionManager.shared.

struct AntiAddictionView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    header

                    if !env.antiAddictionTimer.hasStatus {
                        emptyState
                    } else {
                        if env.antiAddictionTimer.shouldKick {
                            kickBanner
                        } else if env.antiAddictionTimer.shouldWarn {
                            warnBanner
                        }
                        sessionCard
                        todayCard
                        identityCard
                        curfewCard
                    }

                    configCard
                    demoControlsCard
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
    }

    private var timer: AntiAddictionTimerService { env.antiAddictionTimer }

    // MARK: - Header

    private var header: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Chống nghiện")
                .font(VLFont.h1)
                .foregroundColor(VLColor.onSurface)
            Text("Giám sát tuân thủ NĐ 147/2024 (server-driven)")
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurfaceVariant)
            HStack(spacing: VLSpacing.xs) {
                Circle()
                    .fill(timer.sessionActive ? VLColor.success : VLColor.onSurfaceVariant)
                    .frame(width: 8, height: 8)
                Text(timer.sessionActive ? "Phiên chơi đang được giám sát" : "Phiên chưa khởi động")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Spacer()
                Button(action: { timer.refreshNow(); toast.info("Đang cập nhật trạng thái…") }) {
                    Image(systemName: "arrow.clockwise")
                }
                .foregroundColor(VLColor.primary)
            }
            .padding(.top, 4)
        }
    }

    // MARK: - Empty state

    private var emptyState: some View {
        VStack(spacing: VLSpacing.sm) {
            Image(systemName: VLIcon.clock)
                .font(.system(size: 32))
                .foregroundColor(VLColor.onSurfaceVariant)
            Text("Đang chờ trạng thái từ server…")
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurface)
            Text("SDK gọi /anti-addiction/status sau ~60s đầu phiên, sau đó mỗi \(timer.config?.warningIntervalMinutes ?? 30) phút.")
                .font(VLFont.bodySm)
                .foregroundColor(VLColor.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(VLSpacing.lg)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    // MARK: - Banners

    private var warnBanner: some View {
        Banner(tone: VLColor.warning, icon: VLIcon.warning, title: "Cảnh báo nghỉ ngơi",
               message: "Bạn đã chơi liên tục \(timer.currentSessionMinutes) phút. Nghỉ ngơi để bảo vệ sức khỏe.")
    }

    private var kickBanner: some View {
        Banner(tone: VLColor.error, icon: VLIcon.gavel, title: "Hết giờ chơi",
               message: "SDK sẽ đăng xuất theo quy định. Vui lòng quay lại sau.")
    }

    // MARK: - Session card (live countdown)

    private var sessionCard: some View {
        let _ = timer.remainingSessionSeconds  // re-render every tick
        let progress = sessionProgress()
        return Card(title: "PHIÊN HIỆN TẠI", icon: VLIcon.clock) {
            HStack(alignment: .firstTextBaseline) {
                Text(timer.formattedRemainingSession)
                    .font(.system(size: 32, weight: .bold, design: .monospaced))
                    .foregroundColor(VLColor.onSurface)
                Spacer()
                VStack(alignment: .trailing, spacing: 0) {
                    Text("còn lại")
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                    Text("phiên")
                        .font(VLFont.labelBold)
                        .foregroundColor(VLColor.onSurface)
                }
            }
            VLProgressBar(progress: progress)
            HStack {
                Text("Đã chơi \(timer.currentSessionMinutes) phút")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Spacer()
                if let synced = timer.lastSyncAt {
                    Text("Sync \(Self.relative(synced))")
                        .font(VLFont.bodySm)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
            }
        }
    }

    private func sessionProgress() -> Double {
        let total = timer.currentSessionMinutes * 60 + timer.remainingSessionSeconds
        guard total > 0 else { return 0 }
        return Double(timer.currentSessionMinutes * 60) / Double(total)
    }

    // MARK: - Today card

    private var todayCard: some View {
        let played = timer.totalPlayedTodayMinutes
        let remaining = timer.remainingTodayMinutes
        let total = played + remaining
        let progress = total > 0 ? Double(played) / Double(total) : 0
        return Card(title: "HÔM NAY", icon: VLIcon.calendar) {
            HStack {
                Text("\(played) / \(total) phút")
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.onSurface)
                Spacer()
                Text(timer.formattedRemainingToday)
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.primary)
            }
            VLProgressBar(progress: progress)
            Text("Reset 00:00 giờ Việt Nam")
                .font(VLFont.bodySm)
                .foregroundColor(VLColor.onSurfaceVariant)
        }
    }

    // MARK: - Identity card

    private var identityCard: some View {
        Card(title: "DANH TÍNH", icon: VLIcon.badge) {
            VLPropertyRow(label: "Nhóm tuổi", value: timer.ageGroupLabel, icon: VLIcon.calendar)
            Divider()
            VLPropertyRow(label: "User", value: env.currentUser?.userName ?? "—", icon: VLIcon.person)
        }
    }

    // MARK: - Curfew card

    private var curfewCard: some View {
        let cfg = timer.config
        let curfewWindow = cfg.map { "\(twoDigit($0.curfewStartHour)):00 – \(twoDigit($0.curfewEndHour)):00" } ?? "—"
        return Card(title: "GIỜ CẤM", icon: VLIcon.moon) {
            HStack {
                Text(curfewWindow)
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.onSurface)
                Spacer()
                if timer.curfewActive {
                    VLStatePill(text: "ĐANG ÁP DỤNG", tone: .error, icon: VLIcon.lock)
                } else {
                    VLStatePill(text: "KHÔNG", tone: .neutral)
                }
            }
            if let next = timer.nextCurfewStart, !next.isEmpty {
                Text("Lần kế: \(next)")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
            if let cfg, !cfg.curfewAppliesTo.isEmpty {
                Text("Áp dụng: \(cfg.curfewAppliesTo.joined(separator: ", "))")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
    }

    // MARK: - Config card

    private var configCard: some View {
        Card(title: "CẤU HÌNH", icon: VLIcon.tabSettings) {
            if let c = timer.config {
                VLPropertyRow(label: "Trạng thái", value: c.enabled ? "Bật" : "Tắt", icon: VLIcon.shieldOk)
                Divider()
                VLPropertyRow(label: "Tuổi <16: tối đa/ngày",
                              value: minutesText(c.underageMaxMinutesPerDay), icon: VLIcon.calendar)
                VLPropertyRow(label: "Tuổi <16: tối đa/phiên",
                              value: minutesText(c.underageMaxMinutesPerSession), icon: VLIcon.clock)
                Divider()
                VLPropertyRow(label: "Tuổi 16–17: tối đa/ngày",
                              value: minutesText(c.teenMaxMinutesPerDay), icon: VLIcon.calendar)
                VLPropertyRow(label: "Tuổi 16–17: tối đa/phiên",
                              value: minutesText(c.teenMaxMinutesPerSession), icon: VLIcon.clock)
                Divider()
                VLPropertyRow(label: "Tuổi ≥18",
                              value: c.adultMaxMinutesPerDay < 0 ? "Không giới hạn" : minutesText(c.adultMaxMinutesPerDay),
                              icon: VLIcon.calendar)
                Divider()
                VLPropertyRow(label: "Polling status mỗi",
                              value: "\(c.warningIntervalMinutes) phút", icon: VLIcon.clock)
            } else {
                Text("Chưa nhận được config từ server.")
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
    }

    // MARK: - Demo controls

    private var demoControlsCard: some View {
        Card(title: "DEMO CONTROLS", icon: VLIcon.terminal) {
            Button(action: forceWarn) {
                HStack(spacing: 6) {
                    Image(systemName: VLIcon.warning)
                    Text("Force Warn Popup")
                }
            }
            .vlButton(.secondary)

            Button(action: forceKick) {
                HStack(spacing: 6) {
                    Image(systemName: VLIcon.gavel)
                    Text("Force Kick Popup (sign out)")
                }
            }
            .vlButton(.destructive)
        }
    }

    private func forceWarn() {
        AntiAddictionManager.shared().debugForceWarnPopup()
        toast.info("debug: warn popup", "AntiAddictionManager.debugForceWarnPopup() invoked")
    }

    private func forceKick() {
        AntiAddictionManager.shared().debugForceKickPopup()
        toast.warning("debug: kick popup", "Sẽ đăng xuất sau khi đóng (mirror flow prod)")
    }

    // MARK: - Helpers

    private func minutesText(_ m: Int) -> String {
        if m < 0 { return "—" }
        if m >= 60 { return "\(m / 60)h \(m % 60)m" }
        return "\(m)m"
    }

    private func twoDigit(_ n: Int) -> String { String(format: "%02d", n) }

    private static func relative(_ date: Date) -> String {
        let s = Int(Date().timeIntervalSince(date))
        if s < 60 { return "\(s)s trước" }
        if s < 3600 { return "\(s/60)m trước" }
        return "\(s/3600)h trước"
    }
}

// MARK: - Reusable card shell

private struct Card<Content: View>: View {
    let title: String
    let icon: String
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack(spacing: VLSpacing.xs) {
                Image(systemName: icon)
                    .foregroundColor(VLColor.primary)
                Text(title)
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Spacer()
            }
            content()
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }
}

private struct Banner: View {
    let tone: Color
    let icon: String
    let title: String
    let message: String

    var body: some View {
        HStack(alignment: .top, spacing: VLSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 22, weight: .semibold))
                .foregroundColor(tone)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.onSurface)
                Text(message)
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurface)
                    .fixedSize(horizontal: false, vertical: true)
            }
            Spacer()
        }
        .padding(VLSpacing.md)
        .background(tone.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(tone.opacity(0.4), lineWidth: 1)
        )
    }
}
