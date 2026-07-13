import Foundation
import VLPlaySDK

struct TokenRefreshLogEntry: Identifiable {
    let id = UUID()
    let timestamp: Date
    let oldTokenPreview: String
    let newTokenPreview: String
    let expiresAt: Date?
}

final class SDKEnvironment: NSObject, ObservableObject, VLPlaySDKManagerDelegate {
    @Published var currentUser: VIDUser?
    @Published var lastEvent: String = "—"
    @Published var tokenRefreshLog: [TokenRefreshLogEntry] = []

    let toast = VLToastCenter()
    let loading = VLLoadingCenter()
    let confirm = VLConfirmCenter()
    let antiAddictionTimer = AntiAddictionTimerService()

    override init() {
        super.init()
        VLPlaySDKManager.default().delegate = self
        // Hydrate currentUser if SDK already cached one (post-restart)
        let cached = VIDUser.current()
        if cached.signedIn {
            currentUser = cached
        }
    }

    // MARK: - VLPlaySDKManagerDelegate
    func sdkManagerDidSignIn(with user: VIDUser!) {
        DispatchQueue.main.async {
            self.currentUser = user
            self.lastEvent = "signed in as \(user?.userName ?? "?")"
            self.toast.success("Đăng nhập thành công",
                               "Welcome \(user?.userName ?? "back")")
        }
    }

    func sdkManagerDidSignOut() {
        DispatchQueue.main.async {
            self.currentUser = nil
            self.lastEvent = "signed out"
            self.toast.info("Đã đăng xuất")
        }
    }

    func sdkManagerDidPurchaseSuccessfully() {
        DispatchQueue.main.async {
            self.lastEvent = "purchase ok"
            self.toast.success("Mua thành công")
        }
    }

    func sdkManagerDidSessionExpire() {
        DispatchQueue.main.async {
            self.lastEvent = "session expired"
            self.toast.error("Session expired", "Vui lòng đăng nhập lại")
        }
    }

    // MARK: - Demo helpers

    func refreshToken() {
        guard let user = currentUser else { return }
        let oldPreview = preview(of: user.accessToken)
        loading.show("Đang refresh token…")
        VLPlaySDKManager.default().forceRefreshToken { [weak self] status, newAccess, _, error in
            DispatchQueue.main.async {
                guard let self else { return }
                self.loading.hide()
                if status {
                    let exp = VIDUser.current().expiration
                    let entry = TokenRefreshLogEntry(
                        timestamp: Date(),
                        oldTokenPreview: oldPreview,
                        newTokenPreview: self.preview(of: newAccess),
                        expiresAt: exp > 0 ? Date(timeIntervalSince1970: exp) : nil
                    )
                    self.tokenRefreshLog.insert(entry, at: 0)
                    self.currentUser = VIDUser.current()
                    let f = DateFormatter()
                    f.dateFormat = "HH:mm:ss"
                    self.toast.success("Token refreshed", "at \(f.string(from: entry.timestamp))")
                } else {
                    let msg = (error as NSError?)?.userInfo["message"] as? String ?? "Refresh thất bại"
                    self.toast.error("Refresh failed", msg)
                }
            }
        }
    }

    private func preview(of token: String?) -> String {
        guard let t = token, !t.isEmpty else { return "—" }
        let head = t.prefix(8)
        return "\(head)…(\(t.count) chars)"
    }

    // MARK: - Profile refresh
    // Calls VLPlaySDKManager+Internal `refreshAccountInfo:` (forward-declared in
    // bridging header) which refetches GET /api/v1/detail/{userId} and replaces
    // VIDUser.currentUser.detailAccountInfoModel. We reassign self.currentUser
    // so SwiftUI re-renders downstream views.
    func refreshAccountInfo(completion: ((Bool) -> Void)? = nil) {
        guard currentUser != nil else { completion?(false); return }
        loading.show()
        VLPlaySDKManager.default().refreshAccountInfo { [weak self] status, error in
            guard let self else { return }
            self.loading.hide()
            if status {
                self.currentUser = VIDUser.current()
                self.toast.success("Profile refreshed")
            } else {
                let msg = error?.localizedDescription ?? "Network error"
                self.toast.error("Refresh failed", msg)
            }
            completion?(status)
        }
    }
}
