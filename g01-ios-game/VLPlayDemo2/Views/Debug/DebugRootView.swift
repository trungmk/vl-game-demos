import SwiftUI

// Debug tab root. Per Q4: Anti-Addiction status + Token Refresh viewer.

struct DebugRootView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                VLTopAppBar(variant: .debugger, environmentBadge: "STAGING")

                List {
                    Section("Compliance") {
                        NavigationLink {
                            AntiAddictionView()
                        } label: {
                            row(icon: VLIcon.antiAddiction, title: "Anti-Addiction Status", subtitle: "Playtime · Curfew · Demo controls")
                        }
                    }

                    Section("Tokens") {
                        NavigationLink {
                            TokenRefreshLogView()
                        } label: {
                            row(icon: VLIcon.tokenRefresh, title: "Token Refresh Log", subtitle: "Recent token refresh history")
                        }
                    }

                    Section("Remote Config") {
                        NavigationLink {
                            RemoteConfigView()
                        } label: {
                            row(icon: VLIcon.tabSettings, title: "Remote SDK Config", subtitle: "CMS-driven features · payment methods · support")
                        }
                    }
                }
                .listStyle(.insetGrouped)
                .scrollContentBackground(.hidden)
                .background(VLColor.surface)
            }
            .background(VLColor.surface.ignoresSafeArea())
        }
    }

    private func row(icon: String, title: String, subtitle: String) -> some View {
        HStack(spacing: VLSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(VLColor.primary)
                .frame(width: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(VLFont.bodyLg.weight(.semibold))
                    .foregroundColor(VLColor.onSurface)
                Text(subtitle)
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
    }
}
