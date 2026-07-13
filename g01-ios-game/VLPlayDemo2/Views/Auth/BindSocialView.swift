import SwiftUI
import VLPlaySDK

// c3 — Linked Accounts. 3 rows for FB / Google / Apple bind/unlink.

struct BindSocialView: View {
    @EnvironmentObject var env: SDKEnvironment
    @EnvironmentObject var toast: VLToastCenter

    var body: some View {
        // VLTopAppBar dropped — pushed onto MainHubView's NavigationStack so
        // native nav bar provides back; in-view header below carries title.
        ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    Text("Linked Accounts")
                        .font(VLFont.h1)
                        .foregroundColor(VLColor.onSurface)
                    Text("Manage your connected social providers. Bind to enable one-tap login or unlink to revoke access.")
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurfaceVariant)

                    VStack(spacing: VLSpacing.sm) {
                        providerRow(name: "Facebook", code: "F", brandColor: Color(hex: 0x1877F2), linked: false)
                        providerRow(name: "Google",   code: "G", brandColor: VLColor.surfaceContainerLowest, linked: true, brandFgOverride: VLColor.onSurface, outlined: true)
                        providerRow(name: "Apple",    code: "A", brandColor: .black, linked: false)
                    }
                }
                .padding(.horizontal, VLSpacing.safeMargin)
                .padding(.vertical, VLSpacing.md)
            }
        .background(VLColor.surface.ignoresSafeArea())
        .navigationBarBackButtonHidden(false)
    }

    private func providerRow(name: String,
                             code: String,
                             brandColor: Color,
                             linked: Bool,
                             brandFgOverride: Color? = nil,
                             outlined: Bool = false) -> some View {
        HStack(spacing: VLSpacing.md) {
            ZStack {
                RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                    .fill(brandColor)
                    .frame(width: 44, height: 44)
                    .overlay(
                        RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                            .stroke(outlined ? VLColor.outlineVariant : .clear, lineWidth: 1)
                    )
                Text(code)
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(brandFgOverride ?? .white)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(name)
                    .font(VLFont.bodyLg.weight(.semibold))
                    .foregroundColor(VLColor.onSurface)
                if linked {
                    VLStatePill(text: "LINKED", tone: .primary)
                } else {
                    Text("Not linked")
                        .font(VLFont.bodySm)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
            }

            Spacer()

            Button(linked ? "Unlink" : "Bind") {
                onBindTap(name: name, currentlyLinked: linked)
            }
            .vlButton(linked ? .secondary : .primary, fullWidth: false, compact: true)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }

    private func onBindTap(name: String, currentlyLinked: Bool) {
        if currentlyLinked {
            toast.warning("Unlink \(name)", "Stub — real unlink requires SDK API + confirm.")
        } else {
            toast.info("Bind \(name)", "Native OAuth flow → Web SDK exchange (Option B).")
        }
    }
}
