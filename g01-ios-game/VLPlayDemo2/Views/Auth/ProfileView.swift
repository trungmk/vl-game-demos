import SwiftUI
import VLPlaySDK

// Profile inspection — read-only dump of fields the BE actually returns.
// Schema source: vlgame-project-hub/contracts/openapi-v3.0.yaml AuthResponse
//   (data.player: _id, username, email, phone, isGuest, isUpdatePassword)
// + /api/v1/detail/{accountId} (KYC: fullName, dob, identityCard, issueDate,
//   placeOfGrant, isBanned).
// Internal SDK-only fields (userId2 alias, accountUsingMobile, gameState,
// gameVersion, extend, quickPlayUser) are intentionally omitted.

struct ProfileView: View {
    @EnvironmentObject var env: SDKEnvironment
    @State private var lastRefreshedAt: Date?
 
    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(spacing: VLSpacing.md) {
                    avatarBlock
                    refreshBar
                    accountSection
                    identitySection
                    authenticationSection
                    tokensSection
                }
            .padding(.horizontal, VLSpacing.safeMargin)
            .padding(.vertical, VLSpacing.md)
        }
        .background(VLColor.surface.ignoresSafeArea())
    }

    // MARK: - Header

    private var avatarBlock: some View {
        let user = env.currentUser
        return VStack(spacing: VLSpacing.sm) {
            ZStack {
                Circle()
                    .fill(VLColor.surfaceContainerLowest)
                    .frame(width: 92, height: 92)
                    .overlay(Circle().stroke(VLColor.primary, lineWidth: 3))
                Text(initials(user?.userName))
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(VLColor.primary)
            }
            HStack(spacing: VLSpacing.xs) {
                Text("@\(user?.userName ?? "user")")
                    .font(VLFont.h2)
                    .foregroundColor(VLColor.onSurface)
                VLStatePill(text: authBadge(user?.authenType), tone: badgeTone(user?.authenType))
            }
            Text("Player ID  \(playerId(user) ?? "—")")
                .font(VLFont.code)
                .foregroundColor(VLColor.onSurfaceVariant)
                .lineLimit(1)
                .truncationMode(.middle)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, VLSpacing.md)
    }

    private var refreshBar: some View {
        HStack(spacing: VLSpacing.sm) {
            Image(systemName: VLIcon.clock)
                .foregroundColor(VLColor.onSurfaceVariant)
            Text(lastRefreshLabel)
                .font(VLFont.bodyMd)
                .foregroundColor(VLColor.onSurfaceVariant)
            Spacer()
            Button {
                env.refreshAccountInfo { status in
                    if status { lastRefreshedAt = Date() }
                }
            } label: {
                HStack(spacing: VLSpacing.xs) {
                    Image(systemName: "arrow.clockwise")
                    Text("Refresh")
                }
                .font(VLFont.labelBold)
                .foregroundColor(VLColor.primary)
            }
        }
        .padding(.horizontal, VLSpacing.md)
        .padding(.vertical, VLSpacing.sm)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private var lastRefreshLabel: String {
        guard let d = lastRefreshedAt else { return "Cached từ login" }
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss"
        return "Refreshed \(f.string(from: d))"
    }

    // MARK: - Account (AuthResponse.data.player)

    private var accountSection: some View {
        let u = env.currentUser
        let p = u?.loginInfoModel?.data?.player
        let detail = u?.detailAccountInfoModel?.data?.player
        return card("Account", subtitle: "data.player từ AuthResponse") {
            VLPropertyRow(label: "Player ID (_id)",
                          value: playerId(u) ?? "—",
                          icon: VLIcon.badge,
                          copyable: true)
            divider
            VLPropertyRow(label: "Username",
                          value: p?.username ?? u?.userName ?? "—",
                          icon: VLIcon.person,
                          copyable: true)
            divider
            VLPropertyRow(label: "Email",
                          value: detail?.email ?? u?.email ?? "—",
                          icon: VLIcon.email,
                          copyable: true)
            divider
            VLPropertyRow(label: "Phone",
                          value: detail?.phone ?? u?.mobile ?? "—",
                          icon: VLIcon.phone,
                          copyable: true)
            divider
            VLPropertyRow(label: "Is Guest",
                          value: isGuestStr(u),
                          icon: VLIcon.person)
            divider
            VLPropertyRow(label: "Update password required",
                          value: boolStr(p?.isUpdatePassword?.boolValue),
                          icon: VLIcon.lockRotate)
        }
    }

    // MARK: - Identity (KYC) — GET /detail/{accountId}

    private var identitySection: some View {
        let p = env.currentUser?.detailAccountInfoModel?.data?.player
        return card("Identity (KYC)", subtitle: "GET /api/v1/detail/{accountId}") {
            if p == nil {
                Text("Bấm Refresh để tải hồ sơ KYC từ /api/v1/detail/{accountId}")
                    .font(VLFont.bodyMd)
                    .foregroundColor(VLColor.onSurfaceVariant)
                    .padding(.vertical, VLSpacing.xs)
            } else {
                VLPropertyRow(label: "Full name",     value: p?.fullName ?? "—",     icon: VLIcon.person)
                divider
                VLPropertyRow(label: "Date of birth", value: p?.dob ?? "—",          icon: VLIcon.calendar)
                divider
                VLPropertyRow(label: "Identity card", value: p?.identityCard ?? "—", icon: VLIcon.badge, copyable: true)
                divider
                VLPropertyRow(label: "Issue date",    value: p?.issueDate ?? "—",    icon: VLIcon.calendar)
                divider
                VLPropertyRow(label: "Place of grant", value: p?.placeOfGrant ?? "—", icon: VLIcon.tag)
                divider
                VLPropertyRow(label: "Is banned",
                              value: boolStr(p?.isBanned?.boolValue),
                              icon: VLIcon.warning)
            }
        }
    }

    // MARK: - Authentication

    private var authenticationSection: some View {
        let u = env.currentUser
        let data = u?.loginInfoModel?.data
        return card("Authentication", subtitle: "Auth type + login flow") {
            VLPropertyRow(label: "Auth type",  value: authTypeLabel(u?.authenType), icon: VLIcon.shieldOk)
            divider
            VLPropertyRow(label: "Login type", value: data?.type ?? "—",            icon: VLIcon.tag)
            divider
            VLPropertyRow(label: "Signed in",
                          value: (u?.signedIn == true) ? "Yes" : "No",
                          icon: VLIcon.shieldOk)
        }
    }

    // MARK: - Tokens

    private var tokensSection: some View {
        let u = env.currentUser
        return card("Tokens", subtitle: "Lưu trữ trong Keychain") {
            VLPropertyRow(label: "Access token",
                          value: tokenPreview(u?.accessToken),
                          icon: VLIcon.lock,
                          copyable: true)
            divider
            VLPropertyRow(label: "Refresh token",
                          value: tokenPreview(u?.refreshToken),
                          icon: VLIcon.tokenRefresh,
                          copyable: true)
            divider
            VLPropertyRow(label: "Expires at",
                          value: expirationStr(u?.expiration ?? 0),
                          icon: VLIcon.clock)
            if let g = u?.googleSignInToken, !g.isEmpty {
                divider
                VLPropertyRow(label: "Google ID token", value: tokenPreview(g),
                              icon: VLIcon.lock, copyable: true)
            }
            if let a = u?.appleSignInToken, !a.isEmpty {
                divider
                VLPropertyRow(label: "Apple ID token", value: tokenPreview(a),
                              icon: VLIcon.lock, copyable: true)
            }
        }
    }

    // MARK: - Card shell

    private func card<Content: View>(_ title: String, subtitle: String? = nil,
                                     @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: VLSpacing.xs) {
            Text(title)
                .font(VLFont.labelBold)
                .foregroundColor(VLColor.onSurfaceVariant)
            if let s = subtitle {
                Text(s)
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant.opacity(0.7))
                    .padding(.bottom, 2)
            }
            VStack(spacing: 0) { content() }
                .padding(VLSpacing.md)
                .background(VLColor.surfaceContainerLowest)
                .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .stroke(VLColor.outlineVariant, lineWidth: 1)
                )
        }
    }

    private var divider: some View {
        Divider().padding(.leading, 28)
    }

    // MARK: - Helpers

    /// Canonical Mongo ObjectId — VIDUser.userId2 mirrors `data.player._id`.
    /// Fall back to legacy userId if backend hasn't shipped the new shape yet.
    private func playerId(_ u: VIDUser?) -> String? {
        if let id = u?.userId2, !id.isEmpty, id != "0" { return id }
        if let id = u?.userId, !id.isEmpty, id != "0" { return id }
        return nil
    }

    private func isGuestStr(_ u: VIDUser?) -> String {
        guard let raw = u?.authenType.rawValue else { return "—" }
        return raw == 8 ? "Yes" : "No"
    }

    private func initials(_ name: String?) -> String {
        guard let n = name, !n.isEmpty else { return "?" }
        return String(n.prefix(2)).uppercased()
    }

    private func tokenPreview(_ t: String?) -> String {
        guard let t, !t.isEmpty else { return "—" }
        let head = t.prefix(16)
        return "\(head)…(\(t.count) chars)"
    }

    private func expirationStr(_ ts: TimeInterval) -> String {
        guard ts > 0 else { return "—" }
        let date = Date(timeIntervalSince1970: ts)
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return f.string(from: date)
    }

    private func boolStr(_ v: Bool?) -> String {
        guard let v else { return "—" }
        return v ? "Yes" : "No"
    }

    // AuthenType raw values (from VIDUser.h):
    // 0=Normal, 1=Facebook, 2=Google, 3=Yahoo, 7=Apple, 8=QuickStart, -1=None
    private func authTypeLabel(_ t: AuthenType?) -> String {
        guard let raw = t?.rawValue else { return "—" }
        switch raw {
        case 0:  return "GGID (Username/Password)"
        case 1:  return "Facebook"
        case 2:  return "Google"
        case 3:  return "Yahoo"
        case 7:  return "Apple"
        case 8:  return "Guest (Quick Start)"
        default: return "—"
        }
    }

    private func authBadge(_ t: AuthenType?) -> String {
        guard let raw = t?.rawValue else { return "LINKED" }
        switch raw {
        case 8:  return "GUEST"
        case 1:  return "FACEBOOK"
        case 2:  return "GOOGLE"
        case 7:  return "APPLE"
        case 0:  return "GGID"
        default: return "LINKED"
        }
    }

    private func badgeTone(_ t: AuthenType?) -> VLPillTone {
        guard let raw = t?.rawValue else { return .neutral }
        switch raw {
        case 8:  return .tertiary
        case 0:  return .primary
        default: return .neutral
        }
    }
}
