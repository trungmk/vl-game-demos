package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sdk.vlplay.vn.config.SdkConfig
import sdk.vlplay.vn.sample.design.components.AntiAddictionClock
import sdk.vlplay.vn.sample.design.components.VLActionTile
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmModalContent
import sdk.vlplay.vn.sample.design.components.VLConfirmModalVariant
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * Post-auth Dashboard tab content. Parity iOS demo2 [MainHubView].
 *
 * Layout top-to-bottom:
 * 1. SDK_DEBUGGER top app bar with `STAGING` env badge
 * 2. Anti-addiction clock banner (gated by CMS `antiAddiction` flag)
 * 3. Profile card with avatar initials + handle + ID
 * 4. 6-tile action grid (Profile / Shop / Bind / Giftcode / Identity-gated /
 *    Token Refresh)
 * 5. Footer: Logout (Secondary, with confirm modal) + Delete (Destructive)
 *
 * Tiles for screens not yet built (Phase 7-9) toast a "coming-in-Phase-X"
 * notice rather than navigate to dead routes — keeps the demo loop runnable
 * even before Bind/Giftcode/Shop/Identity land.
 *
 * The hub does NOT own a NavHost. Callers (i.e. [MainTabScreen]) pass the
 * outer-graph navigation lambdas; the hub just decides when to fire them.
 */
@Composable
fun MainHubScreen(
    env: SDKEnvironment,
    onProfile: () -> Unit,
    onShop: () -> Unit,
    onBind: () -> Unit,
    onGiftcode: () -> Unit,
    onIdentity: () -> Unit,
    onDeleteAccount: () -> Unit,
    onAntiAddiction: () -> Unit,
    onAFEvents: () -> Unit,
    onAds: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val antiAddictionEnabled =
        VLPlaySDKManager.isFeatureEnabled(SdkConfig.FEATURE_ANTI_ADDICTION)
    val identityEnabled =
        VLPlaySDKManager.isFeatureEnabled(SdkConfig.FEATURE_IDENTITY_VERIFICATION)
    val user = env.currentUser

    Column(modifier = Modifier
        .fillMaxSize()
        .background(VLColor.Surface.copy(alpha = 0.82f)),
    ) {
        VLTopAppBar(
            variant = VLTopBarVariant.Debugger,
            environmentBadge = "STAGING",
            trailing = {
                IconButton(onClick = { env.toast.info("Settings") }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = VLColor.OnSurfaceVariant,
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
            if (antiAddictionEnabled) {
                AntiAddictionClock(
                    state = env.antiAddictionClockState,
                    onClick = onAntiAddiction,
                )
            }

            ProfileCard(
                handle = user?.accountName?.ifEmpty { null } ?: "user",
                accountId = user?.accountId?.takeIf { it.isNotEmpty() } ?: "—",
            )

            ActionGrid(
                identityEnabled = identityEnabled,
                onProfile = onProfile,
                onShop = onShop,
                onBind = onBind,
                onGiftcode = onGiftcode,
                onIdentity = onIdentity,
                onTokenRefresh = {
                    env.toast.info("Token refresh — Android SDK chưa expose hook public; xem log từ Debug → Token Refresh")
                },
                onAFEvents = onAFEvents,
                onAds = onAds,
            )

            FooterButtons(
                onLogout = {
                    env.confirm.ask(
                        content = VLConfirmModalContent(
                            title = "Đăng xuất?",
                            message = "Bạn sẽ phải đăng nhập lại để tiếp tục dùng demo.",
                            confirmTitle = "Đăng xuất",
                            cancelTitle = "Huỷ",
                            variant = VLConfirmModalVariant.Info,
                        ),
                        onConfirm = {
                            if (activity != null) {
                                VLPlaySDKManager.signOut(activity) {
                                    env.refreshUserSnapshot()
                                    onSignedOut()
                                }
                            }
                        },
                    )
                },
                onDelete = onDeleteAccount,
            )

            // Reserve room so the last scrollable item clears the floating
            // tab pill (parity iOS: pill overlays scroll content; bottom
            // inset = pill height + gesture-bar zone + breathing).
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun ProfileCard(handle: String, accountId: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(VLColor.PrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials(handle),
                    style = VLFont.h2,
                    color = VLColor.Primary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
            ) {
                Text(
                    text = "@$handle",
                    style = VLFont.h2,
                    color = VLColor.OnSurface,
                )
                Text(
                    text = "ID $accountId",
                    style = VLFont.code,
                    color = VLColor.OnSurfaceVariant,
                )
            }
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = VLColor.Primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private fun initials(name: String): String =
    if (name.isBlank()) "?" else name.take(2).uppercase()

@Composable
private fun ActionGrid(
    identityEnabled: Boolean,
    onProfile: () -> Unit,
    onShop: () -> Unit,
    onBind: () -> Unit,
    onGiftcode: () -> Unit,
    onIdentity: () -> Unit,
    onTokenRefresh: () -> Unit,
    onAFEvents: () -> Unit,
    onAds: () -> Unit,
) {
    // Demonstrates VLPlaySDKManager.showGuestUpgrade(activity) — a game can wire its
    // own "upgrade account" button to present the SDK's guest-upgrade popup.
    val upgradeActivity = LocalContext.current.findActivity()
    val tiles = buildList {
        add(Tile("Profile", Icons.Filled.AccountCircle, onProfile))
        add(Tile("Shop", Icons.Filled.ShoppingCart, onShop))
        add(Tile("Giftcode", Icons.Filled.CardGiftcard, onGiftcode))
        if (identityEnabled) {
            add(Tile("Xác minh danh tính", Icons.Filled.Badge, onIdentity))
        }
        add(Tile("Nâng cấp TK", Icons.Filled.PersonAdd) {
            upgradeActivity?.let { VLPlaySDKManager.showGuestUpgrade(it) }
        })
        add(Tile("Token Refresh", Icons.Filled.Refresh, onTokenRefresh))
        add(Tile("AF Events", Icons.AutoMirrored.Filled.Send, onAFEvents))
        // IAA test console — provider-agnostic VLPlayAds facade (MAX/AdMob).
        add(Tile("Ads Test", Icons.Filled.Campaign, onAds))
    }

    // Manual responsive grid via Column+Row — avoids nested-scroll headache
    // from LazyVerticalGrid inside parent verticalScroll. Columns scale with
    // screen width to keep tile size compact (parity iOS: 4 cols landscape,
    // 2 cols portrait). Last-row partial fill padded with invisible Spacers
    // so trailing tiles don't stretch.
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = if (screenWidthDp >= 600) 4 else 2
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        tiles.chunked(columns).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
            ) {
                rowTiles.forEach { tile ->
                    Box(modifier = Modifier.weight(1f)) {
                        VLActionTile(
                            icon = tile.icon,
                            label = tile.label,
                            onClick = tile.onClick,
                        )
                    }
                }
                repeat(columns - rowTiles.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class Tile(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun FooterButtons(
    onLogout: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = VLSpacing.md),
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        VLButton(
            text = "Đăng xuất",
            onClick = onLogout,
            kind = VLButtonKind.Secondary,
        )
        VLButton(
            text = "Xóa tài khoản",
            onClick = onDelete,
            kind = VLButtonKind.Destructive,
        )
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Preview(name = "Phone portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Phone landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Preview(name = "Tablet portrait", showBackground = true, widthDp = 600, heightDp = 1024)
@Composable
private fun MainHubScreenPreview() {
    VLTheme {
        MainHubScreen(
            env = SDKEnvironment(),
            onProfile = {},
            onShop = {},
            onBind = {},
            onGiftcode = {},
            onIdentity = {},
            onDeleteAccount = {},
            onAntiAddiction = {},
            onAFEvents = {},
            onAds = {},
            onSignedOut = {},
        )
    }
}
