import SwiftUI

// Token Refresh history viewer (Q4 + Q5 designed by Claude).
// Sub-screen reachable from Debug tab → "Token refresh log".

struct TokenRefreshLogView: View {
    @EnvironmentObject var env: SDKEnvironment

    var body: some View {
        // VLTopAppBar dropped — pushed view; native nav bar provides back.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Token Refresh Log")
                            .font(VLFont.h1)
                            .foregroundColor(VLColor.onSurface)
                        Text("Each tap on Token Refresh in Dashboard appends an entry here.")
                            .font(VLFont.bodyMd)
                            .foregroundColor(VLColor.onSurfaceVariant)
                    }

                    if env.tokenRefreshLog.isEmpty {
                        VLFooterBanner(
                            icon: VLIcon.info,
                            text: "No refresh recorded yet. Trigger a refresh from Dashboard → Token Refresh tile.",
                            tone: .info
                        )
                    } else {
                        VStack(spacing: VLSpacing.sm) {
                            ForEach(env.tokenRefreshLog) { entry in
                                entryCard(entry)
                            }
                        }
                    }
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
    }

    private func entryCard(_ entry: TokenRefreshLogEntry) -> some View {
        VStack(alignment: .leading, spacing: VLSpacing.xs) {
            HStack {
                Image(systemName: VLIcon.tokenRefresh)
                    .foregroundColor(VLColor.primary)
                Text(format(entry.timestamp))
                    .font(VLFont.labelBold)
                    .foregroundColor(VLColor.onSurfaceVariant)
                Spacer()
                if let exp = entry.expiresAt {
                    VLStatePill(text: "exp \(formatTime(exp))", tone: .neutral)
                }
            }
            Divider().padding(.vertical, VLSpacing.xs)
            VLPropertyRow(label: "Old token", value: entry.oldTokenPreview, icon: VLIcon.lock,        copyable: true)
            VLPropertyRow(label: "New token", value: entry.newTokenPreview, icon: VLIcon.tokenRefresh, copyable: true)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private func format(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return f.string(from: d)
    }

    private func formatTime(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss"
        return f.string(from: d)
    }
}
