import SwiftUI
import VLPlaySDK

// b1 — Post-auth Dashboard. Profile card + 2×4 action grid + footer buttons.
// Mail + Password buttons dropped per Q1 (already internal-only after P3-01).

struct MainHubView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter
    @EnvironmentObject var confirm: VLConfirmCenter
    @State private var showDelete = false
    @State private var goGiftcode = false
    @State private var goShop = false
    @State private var goProfile = false
    @State private var goIdentity = false
    @State private var goAntiAddiction = false
    @State private var goAFEvents = false
    @State private var goAds = false

    private let cols = [GridItem(.flexible(), spacing: 12),
                        GridItem(.flexible(), spacing: 12),
                        GridItem(.flexible(), spacing: 12),
                        GridItem(.flexible(), spacing: 12)]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                VLTopAppBar(variant: .debugger, environmentBadge: "STAGING") {
                    Button { toast.info("Settings") } label: {
                        Image(systemName: VLIcon.settings)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(VLColor.onSurfaceVariant)
                    }
                }

                ScrollView {
                    VStack(spacing: VLSpacing.md) {
                        if VLPlaySDKManager.isFeatureEnabled(VLPlaySDKFeatureAntiAddiction) {
                            antiAddictionBanner
                        }
                        profileCard
                        actionGrid
                        footerButtons
                    }
                    .padding(.horizontal, VLSpacing.safeMargin)
                    .padding(.vertical, VLSpacing.md)
                }
            }
            .background(VLColor.surface.ignoresSafeArea())
            .navigationDestination(isPresented: $showDelete) { DeleteAccountView() }
            .navigationDestination(isPresented: $goGiftcode) { GiftcodeRedeemView() }
            .navigationDestination(isPresented: $goShop)     { ShopView() }
            .navigationDestination(isPresented: $goProfile)  { ProfileView() }
            .navigationDestination(isPresented: $goIdentity) { IdentityVerificationView() }
            .navigationDestination(isPresented: $goAntiAddiction) { AntiAddictionView() }
            .navigationDestination(isPresented: $goAFEvents) { AppsFlyerEventsView() }
            .navigationDestination(isPresented: $goAds) { AdsTestView() }
        }
    }

    // MARK: - Anti-addiction clock (server-driven)
    @ViewBuilder
    private var antiAddictionBanner: some View {
        Button { goAntiAddiction = true } label: {
            AntiAddictionClock(timer: env.antiAddictionTimer)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Profile card
    private var profileCard: some View {
        let user = env.currentUser
        return HStack(spacing: VLSpacing.md) {
            ZStack {
                Circle()
                    .fill(VLColor.primaryContainer)
                    .frame(width: 56, height: 56)
                Text(initials(from: user?.userName))
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.primary)
            }
            VStack(alignment: .leading, spacing: VLSpacing.xs) {
                Text("@\(user?.userName ?? "user")")
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.onSurface)
                Text("ID \(user?.userId ?? "—")")
                    .font(VLFont.code)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
            Spacer()
            Image(systemName: "gamecontroller.fill")
                .font(.system(size: 22, weight: .medium))
                .foregroundColor(VLColor.primary)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private func initials(from name: String?) -> String {
        guard let n = name, !n.isEmpty else { return "?" }
        return String(n.prefix(2)).uppercased()
    }

    // MARK: - Action grid — feature-gated tiles by remote config
    private var actionGrid: some View {
        LazyVGrid(columns: cols, spacing: VLSpacing.sm) {
            VLActionTile(icon: VLIcon.profile,       label: "Profile")     { goProfile = true }
            VLActionTile(icon: VLIcon.shop,          label: "Shop")        { goShop = true }
            VLActionTile(icon: VLIcon.giftcode,      label: "Giftcode")    { goGiftcode = true }
            if VLPlaySDKManager.isFeatureEnabled(VLPlaySDKFeatureIdentityVerification) {
                // Demonstrates the public SDK entry point a game can call from its own
                // button: presents the SDK's identity-verification popup directly.
                VLActionTile(icon: VLIcon.identityCCCD, label: "Xác minh danh tính") { VLPlaySDKManager.showIdentityVerification() }
            }
            // Demonstrates VLPlaySDKManager.showGuestUpgrade() — a game can wire its own
            // "upgrade account" button to present the SDK's guest-upgrade popup.
            VLActionTile(icon: VLIcon.profile, label: "Nâng cấp TK") { VLPlaySDKManager.showGuestUpgrade() }
            VLActionTile(icon: VLIcon.tokenRefresh,  label: "Token Refresh", action: tokenRefreshAction)
            VLActionTile(icon: VLIcon.tag,           label: "AF Events")    { goAFEvents = true }
            VLActionTile(icon: VLIcon.tag,           label: "Ads Test")     { goAds = true }
            VLActionTile(icon: VLIcon.profile,       label: "Mở FAB HUD",   action: openFabHudAction)
        }
    }

    private func tokenRefreshAction() {
        env.refreshToken()
    }

    /// Demo wire-up for NATIVE-FAB Phase 2 public API. Production games would
    /// feed real `setContext` values from their server / character picker —
    /// these placeholders satisfy the Support tab's ≥ 6-char guard so the
    /// WebView loads instead of the "vào game / chọn nhân vật" fallback.
    private func openFabHudAction() {
        VLPlaySDKManager.setContext([
            "serverGameId": "demo2-server-01",
            "serverName":   "Demo Server 1",
            "roleId":       "demo2-role-01",
            "roleName":     "DemoHero01",
        ])
        VLPlaySDKManager.showHud()
    }

    // MARK: - Footer buttons
    private var footerButtons: some View {
        VStack(spacing: VLSpacing.sm) {
            Button("Logout") {
                confirm.ask(VLConfirmModalContent(
                    title: "Đăng xuất?",
                    message: "Bạn sẽ phải đăng nhập lại để tiếp tục dùng demo.",
                    confirmTitle: "Đăng xuất",
                    cancelTitle: "Huỷ",
                    variant: .info
                ), onConfirm: {
                    VLPlaySDKManager.default().signOut()
                })
            }
            .vlButton(.secondary)

            Button("Delete Account") { showDelete = true }
                .vlButton(.destructive)
        }
        .padding(.top, VLSpacing.md)
    }
}
