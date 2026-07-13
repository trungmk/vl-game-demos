import SwiftUI

// Circular countdown clock for anti-addiction session.
// Reads from AntiAddictionTimerService — repaints every second via @Published
// remainingSessionSeconds. Center shows HH:mm:ss; ring fills as time elapses.
// Right column shows "hôm nay còn" + age group + chevron hint to detail.

struct AntiAddictionClock: View {
    @ObservedObject var timer: AntiAddictionTimerService

    private var tone: Color {
        if timer.shouldKick { return VLColor.error }
        if timer.shouldWarn { return VLColor.warning }
        return VLColor.primary
    }

    /// Elapsed fraction of the current session (0…1). When server-known total is
    /// 0 we still draw a thin track so the clock isn't blank.
    private var progress: Double {
        let total = timer.currentSessionMinutes * 60 + timer.remainingSessionSeconds
        guard total > 0 else { return 0 }
        return min(1, max(0, Double(timer.currentSessionMinutes * 60) / Double(total)))
    }

    var body: some View {
        // Subscribe to live values so the view re-renders every tick.
        let _ = timer.remainingSessionSeconds

        HStack(spacing: VLSpacing.md) {
            ring
            VStack(alignment: .leading, spacing: 6) {
                badge(text: badgeText, tone: tone)
                VStack(alignment: .leading, spacing: 1) {
                    Text(timer.hasStatus ? "Hôm nay còn" : "Phiên chơi")
                        .font(VLFont.labelMd)
                        .foregroundColor(VLColor.onSurfaceVariant)
                    Text(timer.hasStatus ? timer.formattedRemainingToday : "Chống nghiện ON")
                        .font(VLFont.h2)
                        .foregroundColor(VLColor.onSurface)
                }
                if timer.hasStatus && !timer.ageGroup.isEmpty {
                    HStack(spacing: 4) {
                        Image(systemName: VLIcon.badge)
                            .font(.system(size: 11))
                        Text(timer.ageGroupLabel)
                            .font(VLFont.bodySm)
                    }
                    .foregroundColor(VLColor.onSurfaceVariant)
                }
                if timer.curfewActive {
                    HStack(spacing: 4) {
                        Image(systemName: VLIcon.moon)
                            .font(.system(size: 11))
                        Text("Đang trong giờ cấm")
                            .font(VLFont.bodySm)
                    }
                    .foregroundColor(VLColor.error)
                }
            }
            Spacer(minLength: 0)
            Image(systemName: "chevron.right")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(VLColor.onSurfaceVariant)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(tone.opacity(0.35), lineWidth: 1)
        )
    }

    private var badgeText: String {
        if !timer.hasStatus { return "ĐANG CHỜ SERVER" }
        if timer.shouldKick { return "HẾT GIỜ" }
        if timer.shouldWarn { return "NÊN NGHỈ" }
        return "ĐANG GIÁM SÁT"
    }

    // MARK: - Ring

    private var ring: some View {
        ZStack {
            // Track
            Circle()
                .stroke(tone.opacity(0.15), lineWidth: 8)
            // Fill = elapsed portion (rotating clockwise from 12 o'clock)
            Circle()
                .trim(from: 0, to: progress)
                .stroke(tone, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .animation(.linear(duration: 1), value: progress)
            // Center text
            VStack(spacing: 0) {
                Text(timer.hasStatus ? timer.formattedRemainingSession : "--:--:--")
                    .font(.system(size: 18, weight: .bold, design: .monospaced))
                    .foregroundColor(VLColor.onSurface)
                    .monospacedDigit()
                Text(timer.hasStatus ? "còn lại" : "đang tải")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
        .frame(width: 96, height: 96)
    }

    private func badge(text: String, tone: Color) -> some View {
        HStack(spacing: 4) {
            Circle()
                .fill(tone)
                .frame(width: 6, height: 6)
            Text(text)
                .font(VLFont.labelBold)
                .foregroundColor(tone)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 3)
        .background(tone.opacity(0.12))
        .clipShape(Capsule())
    }
}
