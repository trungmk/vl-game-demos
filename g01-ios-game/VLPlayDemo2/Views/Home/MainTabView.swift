import SwiftUI

// Post-auth 4-tab container per mockup info architecture.

struct MainTabView: View {
    @State private var selection: Tab = .dashboard

    enum Tab { case dashboard, logs, debug }

    var body: some View {
        TabView(selection: $selection) {
            MainHubView()
                .tabItem { Label("Dashboard", systemImage: VLIcon.tabDashboard) }
                .tag(Tab.dashboard)

            PurchaseHistoryView()
                .tabItem { Label("Logs", systemImage: VLIcon.tabLogs) }
                .tag(Tab.logs)

            DebugRootView()
                .tabItem { Label("Debug", systemImage: VLIcon.tabDebug) }
                .tag(Tab.debug)
        }
        .tint(VLColor.primary)
    }
}
