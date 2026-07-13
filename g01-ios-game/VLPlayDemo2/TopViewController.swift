import UIKit

extension UIApplication {
    /// Walks the key window's root view controller to the top-most presented controller,
    /// so SDK calls that require a `UIViewController` can present on it.
    func topViewController() -> UIViewController? {
        guard
            let scene = connectedScenes.first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene
                ?? connectedScenes.compactMap({ $0 as? UIWindowScene }).first,
            let root = scene.windows.first(where: \.isKeyWindow)?.rootViewController
                ?? scene.windows.first?.rootViewController
        else { return nil }

        var current = root
        while let presented = current.presentedViewController {
            current = presented
        }
        return current
    }
}
