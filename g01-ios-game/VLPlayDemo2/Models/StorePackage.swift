import Foundation

// Mirrors openapi-v3.0.yaml `Package` schema with v2.1 fallback.
// Built from raw NSDictionary returned by VLPlaySDKManager.getProductCatalog.

struct StorePackageItem: Hashable {
    let itemId: String
    let itemName: String
    let quantity: Int

    static func from(_ dict: [String: Any]) -> StorePackageItem? {
        let itemId = (dict["itemId"] as? String) ?? ""
        let itemName = (dict["itemName"] as? String) ?? ""
        let quantity = (dict["quantity"] as? NSNumber)?.intValue ?? 0
        guard !itemName.isEmpty || !itemId.isEmpty || quantity > 0 else { return nil }
        return StorePackageItem(itemId: itemId, itemName: itemName, quantity: quantity)
    }
}

struct StorePackage: Identifiable, Hashable {
    let id: String              // packageId (or storeProductId fallback)
    let storeProductId: String  // value passed to SKPaymentQueue / purchasePackage
    let name: String
    let description: String?
    let priceVND: Int
    let currency: String
    let items: [StorePackageItem]
    let isActive: Bool
    let sortOrder: Int

    /// Parses one entry from BE response.
    /// Tolerates both v3.0 (`storeProductId`, `name`, `items[]`) and legacy
    /// v2.1 (`productId`, `productName`) shapes — whichever BE STG ships.
    static func from(_ dict: [String: Any]) -> StorePackage? {
        let storeProductId =
            (dict["storeProductId"] as? String)
            ?? (dict["productId"] as? String)
            ?? ""
        guard !storeProductId.isEmpty else { return nil }

        let packageId = (dict["packageId"] as? String) ?? storeProductId
        let name =
            (dict["name"] as? String)
            ?? (dict["productName"] as? String)
            ?? storeProductId
        let description = dict["description"] as? String
        let price = (dict["price"] as? NSNumber)?.intValue ?? 0
        let currency = (dict["currency"] as? String) ?? "VND"
        let isActive = (dict["isActive"] as? NSNumber)?.boolValue ?? true
        let sortOrder = (dict["sortOrder"] as? NSNumber)?.intValue ?? 0

        let itemsRaw = dict["items"] as? [[String: Any]] ?? []
        let items = itemsRaw.compactMap(StorePackageItem.from)

        return StorePackage(
            id: packageId,
            storeProductId: storeProductId,
            name: name,
            description: description,
            priceVND: price,
            currency: currency,
            items: items,
            isActive: isActive,
            sortOrder: sortOrder
        )
    }

    /// Sum of all `items[].quantity` — used as the "rewards" headline number on
    /// the package card. Returns 0 when BE didn't ship items[] (v2.1 catalog).
    var totalItemQuantity: Int {
        items.reduce(0) { $0 + $1.quantity }
    }

    /// Best-effort headline label for the rewards line.
    /// Falls back to "x N" when item names are absent.
    var rewardsHeadline: String {
        if let first = items.first, !first.itemName.isEmpty {
            let rest = items.count > 1 ? " +\(items.count - 1) more" : ""
            return "\(first.quantity.formatted()) \(first.itemName)\(rest)"
        }
        if totalItemQuantity > 0 {
            return "\(totalItemQuantity.formatted()) items"
        }
        return description ?? ""
    }
}
