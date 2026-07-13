package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.payment.StorePackage
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLPackageCard
import sdk.vlplay.vn.sample.design.components.VLPackageCardData
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * Shop / store catalog screen. Parity iOS demo2 [ShopView].
 *
 * Wires [VLPlaySDKManager.getProductCatalog] → `GET /api/v1/client/purchase/products`
 * and [VLPlaySDKManager.purchasePackage] → BillingClient + V3 2-step flow per Chen 2026-05-04.
 *
 * No mock packages. Empty/error states surface real catalog problems
 * (typically: BE forgot to set `storeProductId` for the game's packages,
 * or `storeProductId` does not match a published Play Console product).
 *
 * Most-popular badge: BE doesn't ship the flag yet → middle entry of a
 * 3+ package list gets it heuristically (parity iOS).
 */
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    onHistory: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var state by remember { mutableStateOf<ShopState>(ShopState.Idle) }
    var purchasing by remember { mutableStateOf(false) }

    val isSignedIn = env.currentUser?.let {
        it.accessTokenLength > 0 && it.accountId.isNotEmpty()
    } == true

    val loadCatalog: () -> Unit = {
        if (!isSignedIn) {
            state = ShopState.Error("Cần đăng nhập trước khi xem gói nạp.")
        } else {
            state = ShopState.Loading
            VLPlaySDKManager.getProductCatalog(object : VLPlaySDKManager.ProductCatalogListener {
                override fun onSuccess(packages: MutableList<StorePackage>) {
                    val active = packages.filter { it.isActive }
                        .sortedBy { it.sortOrder }
                    state = if (active.isEmpty()) ShopState.Empty else ShopState.Loaded(active)
                }

                override fun onFailure(message: String?, errorCode: Int) {
                    state = ShopState.Error(message ?: "Network error ($errorCode)")
                }
            })
        }
    }

    LaunchedEffect(isSignedIn) {
        if (state is ShopState.Idle) loadCatalog()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Store")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Header(
                    onHistory = onHistory,
                    onRestore = {
                        restore(activity = activity, env = env)
                    },
                )

                when (val s = state) {
                    ShopState.Idle, ShopState.Loading -> LoadingPlaceholder()
                    ShopState.Empty -> EmptyState(onRetry = loadCatalog)
                    is ShopState.Error -> ErrorState(message = s.message, onRetry = loadCatalog)
                    is ShopState.Loaded -> PackageList(
                        packages = s.packages,
                        purchasing = purchasing,
                        onBuy = { pkg ->
                            buy(
                                activity = activity,
                                env = env,
                                pkg = pkg,
                                setPurchasing = { purchasing = it },
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(VLSpacing.md))
                VLButton(
                    text = "← Quay lại",
                    onClick = onBack,
                    kind = VLButtonKind.Ghost,
                )
            }
        }

        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

@Composable
private fun Header(onHistory: () -> Unit, onRestore: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Store", style = VLFont.h1, color = VLColor.OnSurface)
            Text(
                text = "Top up your balance",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
            )
        }
        VLButton(
            text = "Khôi phục",
            onClick = onRestore,
            kind = VLButtonKind.Ghost,
            fullWidth = false,
            compact = true,
        )
        Spacer(modifier = Modifier.size(VLSpacing.xs))
        VLButton(
            text = "History",
            onClick = onHistory,
            kind = VLButtonKind.Secondary,
            fullWidth = false,
            compact = true,
        )
    }
}

@Composable
private fun LoadingPlaceholder() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = VLSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        CircularProgressIndicator(color = VLColor.Primary)
        Text(
            text = "Đang tải gói nạp…",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = VLSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = VLColor.OnSurfaceVariant,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "Chưa có gói nạp khả dụng",
            style = VLFont.bodyLg,
            color = VLColor.OnSurface,
        )
        Text(
            text = "Backend chưa publish package nào cho game này.",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VLButton(
            text = "Tải lại",
            onClick = onRetry,
            kind = VLButtonKind.Secondary,
            fullWidth = false,
            compact = true,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = VLSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = VLColor.Warning,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "Không tải được catalog",
            style = VLFont.bodyLg,
            color = VLColor.OnSurface,
        )
        Text(
            text = message,
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VLButton(
            text = "Thử lại",
            onClick = onRetry,
            kind = VLButtonKind.Secondary,
            fullWidth = false,
            compact = true,
        )
    }
}

@Composable
private fun PackageList(
    packages: List<StorePackage>,
    purchasing: Boolean,
    onBuy: (StorePackage) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.md)) {
        packages.forEachIndexed { index, pkg ->
            val data = pkg.toCardData(mostPopular = isMostPopular(index, packages.size))
            VLPackageCard(
                data = data,
                onBuy = { if (!purchasing) onBuy(pkg) },
                modifier = Modifier.alpha(if (purchasing) 0.6f else 1f),
            )
        }
    }
}

private fun isMostPopular(index: Int, total: Int): Boolean =
    total >= 3 && index == total / 2

private fun StorePackage.toCardData(mostPopular: Boolean): VLPackageCardData {
    val rewards = items.firstOrNull()?.let { item ->
        listOfNotNull(
            item.quantity.takeIf { it > 0 }?.toString(),
            item.itemName.takeIf { it.isNotEmpty() },
        ).joinToString(" ")
    } ?: (description.orEmpty())
    return VLPackageCardData(
        id = id,
        productId = storeProductId,
        title = name,
        rewardsHeadline = rewards,
        priceLabel = VLPackageCardData.formatPriceVND(priceVND, currency),
        mostPopular = mostPopular,
    )
}

private fun buy(
    activity: Activity?,
    env: SDKEnvironment,
    pkg: StorePackage,
    setPurchasing: (Boolean) -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    setPurchasing(true)
    env.loading.show("Đang mở Google Play…")
    // Demo packs are all CONSUMABLE (gem packs). Partner games selling
    // non-consumable products (premium unlock, lifetime VIP) must pass
    // isConsumable=false so the SDK calls acknowledgePurchase instead of
    // consumeAsync after BE captures.
    VLPlaySDKManager.purchasePackage(
        pkg.storeProductId,
        pkg.name,
        pkg.priceVND,
        /* isConsumable = */ true,
        /* extraData    = */ null,
        activity,
        object : VLPlaySDKManager.PurchaseListenerV3 {
            override fun onSuccess(transactionId: String, productId: String, deliveryPending: Boolean) {
                env.loading.hide()
                setPurchasing(false)
                if (deliveryPending) {
                    env.toast.warning(
                        "Đơn hàng đã ghi nhận",
                        "BE đang chờ giao hàng (1036). Sẽ retry tự động.",
                    )
                } else {
                    env.toast.success("Mua thành công", pkg.name)
                }
            }

            override fun onFailure(message: String?, errorCode: Int) {
                env.loading.hide()
                setPurchasing(false)
                env.toast.error("Mua thất bại", message ?: "Mã lỗi $errorCode")
            }

            override fun onCancel() {
                env.loading.hide()
                setPurchasing(false)
                env.toast.info("Đã huỷ", "Bạn đã huỷ giao dịch")
            }
        },
    )
}

private fun restore(activity: Activity?, env: SDKEnvironment) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang khôi phục giao dịch…")
    val restored = mutableListOf<String>()
    VLPlaySDKManager.restorePurchases(activity, object : VLPlaySDKManager.RestoreListener {
        override fun onRestored(productId: String, transactionId: String) {
            restored += productId
        }
        override fun onCompleted(restoredCount: Int) {
            env.loading.hide()
            if (restoredCount > 0) {
                env.toast.success(
                    "Khôi phục thành công",
                    "Đã khôi phục $restoredCount giao dịch: ${restored.joinToString()}",
                )
            } else {
                env.toast.info("Không có giao dịch", "Không có giao dịch để khôi phục.")
            }
        }
        override fun onFailure(message: String?, errorCode: Int) {
            env.loading.hide()
            env.toast.error("Khôi phục thất bại", message ?: "Mã lỗi $errorCode")
        }
    })
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private sealed interface ShopState {
    data object Idle : ShopState
    data object Loading : ShopState
    data object Empty : ShopState
    data class Loaded(val packages: List<StorePackage>) : ShopState
    data class Error(val message: String) : ShopState
}

@Preview(name = "Phone portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Phone landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Preview(name = "Tablet portrait", showBackground = true, widthDp = 600, heightDp = 1024)
@Composable
private fun ShopScreenPreview() {
    VLTheme {
        ShopScreen(onBack = {}, onHistory = {})
    }
}
