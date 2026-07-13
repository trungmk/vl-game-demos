package sdk.vlplay.vn.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.payment.TransactionItem
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.components.VLTransactionCard
import sdk.vlplay.vn.sample.design.components.VLTransactionCardData
import sdk.vlplay.vn.sample.design.components.VLTransactionStatus
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.tracking.VLPlaySDKManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Purchase history screen. Parity iOS demo2 [PurchaseHistoryView].
 *
 * Wires [VLPlaySDKManager.listSuccessfulTransactions] →
 * `GET /api/v1/client/purchase/?page&limit` (Apple/Google IAP per Chen
 * 2026-05-04). Status code 1049 TRANSACTION_NOT_FOUND is collapsed to an
 * empty list at SDK level — no error toast on first-time players.
 *
 * Hosts (toast/loading/confirm) are gated behind [mountHosts] so this
 * screen can be embedded inside [MainTabScreen]'s Logs tab (which already
 * mounts the centers) without double-mounting; when navigated as a
 * standalone route from [ShopScreen], pass `mountHosts = true`.
 */
@Composable
fun PurchaseHistoryScreen(
    onBack: () -> Unit,
    mountHosts: Boolean = true,
    env: SDKEnvironment = viewModel(),
) {
    var state by remember { mutableStateOf<HistoryState>(HistoryState.Idle) }

    val load: () -> Unit = {
        if (state !is HistoryState.Loading) {
            state = HistoryState.Loading
            VLPlaySDKManager.listSuccessfulTransactions(1, 50, object :
                VLPlaySDKManager.PurchaseHistoryListener {
                override fun onSuccess(
                    items: MutableList<TransactionItem>,
                    total: Int,
                    page: Int,
                    limit: Int,
                ) {
                    val cards = items.map { it.toCardData() }
                    state = if (cards.isEmpty()) HistoryState.Empty else HistoryState.Loaded(cards)
                }

                override fun onFailure(message: String?, errorCode: Int) {
                    state = HistoryState.Error(message ?: "Network error ($errorCode)")
                }
            })
        }
    }

    LaunchedEffect(Unit) { if (state is HistoryState.Idle) load() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(
                variant = VLTopBarVariant.Debugger,
                environmentBadge = "STAGING",
                trailing = {
                    IconButton(onClick = load) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = VLColor.Primary,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Header()

                when (val s = state) {
                    HistoryState.Idle, HistoryState.Loading -> LoadingPlaceholder()
                    HistoryState.Empty -> EmptyState()
                    is HistoryState.Error -> ErrorState(message = s.message, onRetry = load)
                    is HistoryState.Loaded -> TransactionList(s.items)
                }

                Spacer(modifier = Modifier.height(VLSpacing.md))
                VLButton(
                    text = "← Quay lại",
                    onClick = onBack,
                    kind = VLButtonKind.Ghost,
                )

                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        if (mountHosts) {
            VLToastHost(center = env.toast)
            VLLoadingOverlay(center = env.loading)
            VLConfirmHost(center = env.confirm)
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Purchase History",
            style = VLFont.h1,
            color = VLColor.OnSurface,
        )
        Text(
            text = "GET /api/v1/client/purchase/",
            style = VLFont.code,
            color = VLColor.OnSurfaceVariant,
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
            text = "Đang tải lịch sử…",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = VLSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = VLColor.OnSurfaceVariant,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "Chưa có giao dịch nào",
            style = VLFont.bodyLg,
            color = VLColor.OnSurface,
        )
        Text(
            text = "Khi mua thành công, giao dịch sẽ hiển thị tại đây.",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            textAlign = TextAlign.Center,
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
            tint = VLColor.Error,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "Lỗi khi tải lịch sử",
            style = VLFont.bodyLg,
            color = VLColor.OnSurface,
        )
        Text(
            text = message,
            style = VLFont.bodySm,
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
private fun TransactionList(items: List<VLTransactionCardData>) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.md)) {
        items.forEach { tx -> VLTransactionCard(data = tx) }
    }
}

private fun TransactionItem.toCardData(): VLTransactionCardData {
    val title = packageName.ifEmpty { productId }.ifEmpty { "Giao dịch" }
    val amountLabel = formatAmount(amount, currency)
    val datetime = formatDatetime(createdAt, createdAtIso)
    val purchaseCode = code.takeIf { it.isNotEmpty() } ?: transactionId
    val statusEnum = when (paymentStatus.lowercase(Locale.US)) {
        "success", "completed", "ok", "delivery_pending" -> VLTransactionStatus.Success
        "failed", "error", "rejected" -> VLTransactionStatus.Failed
        else -> VLTransactionStatus.Pending
    }
    return VLTransactionCardData(
        title = title,
        amount = amountLabel,
        datetime = datetime,
        status = statusEnum,
        purchaseCode = purchaseCode.takeIf { it.isNotEmpty() },
    )
}

private fun formatAmount(amount: Long, currency: String): String {
    if (amount <= 0L) return "—"
    val nf = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${nf.format(amount)} $currency"
}

private fun formatDatetime(epochMillis: Long, fallbackIso: String): String {
    if (epochMillis > 0L) {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return fmt.format(Date(epochMillis))
    }
    return fallbackIso.ifEmpty { "—" }
}

private sealed interface HistoryState {
    data object Idle : HistoryState
    data object Loading : HistoryState
    data object Empty : HistoryState
    data class Loaded(val items: List<VLTransactionCardData>) : HistoryState
    data class Error(val message: String) : HistoryState
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PurchaseHistoryScreenPreview() {
    VLTheme {
        PurchaseHistoryScreen(onBack = {})
    }
}
