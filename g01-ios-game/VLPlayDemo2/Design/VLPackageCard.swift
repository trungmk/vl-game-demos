import SwiftUI

// Shop package card (d1). Hero icon + rewards headline + price + Buy button.
// `mostPopular` shows badge ribbon + filled primary Buy button.

struct VLPackageCardData: Identifiable {
    let id: String                  // matches StorePackage.id
    let productId: String           // storeProductId — passed to SKPaymentQueue
    let title: String
    let rewardsHeadline: String     // e.g. "100 Gems" / "1 Starter Crate"
    let priceLabel: String          // pre-formatted, e.g. "20.000 VND" or "$0.99"
    let mostPopular: Bool
}

struct VLPackageCard: View {
    let data: VLPackageCardData
    var onBuy: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack(alignment: .top) {
                ZStack {
                    RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                        .fill(VLColor.primaryContainer)
                        .frame(width: 56, height: 56)
                    Image(systemName: VLIcon.diamond)
                        .font(.system(size: 26, weight: .semibold))
                        .foregroundColor(VLColor.primary)
                }
                Spacer()
                if data.mostPopular {
                    VLStatePill(text: "MOST POPULAR", tone: .primary)
                }
            }

            Text(data.title)
                .font(VLFont.h2)
                .foregroundColor(VLColor.onSurface)

            if !data.rewardsHeadline.isEmpty {
                HStack(spacing: VLSpacing.xs) {
                    Image(systemName: VLIcon.gem)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(VLColor.primary)
                    Text(data.rewardsHeadline)
                        .font(VLFont.bodyMd)
                        .foregroundColor(VLColor.onSurface)
                }
            }

            Text(data.priceLabel)
                .font(VLFont.h2)
                .foregroundColor(VLColor.onSurface)

            Button(data.mostPopular ? "Buy Premium" : "Buy", action: onBuy)
                .vlButton(data.mostPopular ? .primary : .secondary)
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(data.mostPopular ? VLColor.primary : VLColor.outlineVariant,
                        lineWidth: data.mostPopular ? 1.5 : 1)
        )
    }
}

extension VLPackageCardData {
    static func make(from pkg: StorePackage, mostPopular: Bool = false) -> VLPackageCardData {
        VLPackageCardData(
            id: pkg.id,
            productId: pkg.storeProductId,
            title: pkg.name,
            rewardsHeadline: pkg.rewardsHeadline,
            priceLabel: formatPrice(pkg.priceVND, currency: pkg.currency),
            mostPopular: mostPopular
        )
    }

    private static func formatPrice(_ amount: Int, currency: String) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        let str = formatter.string(from: NSNumber(value: amount)) ?? "\(amount)"
        return "\(str) \(currency)"
    }
}
