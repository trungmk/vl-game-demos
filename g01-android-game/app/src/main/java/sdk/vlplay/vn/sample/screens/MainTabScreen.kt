package sdk.vlplay.vn.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment

/**
 * Post-auth 3-tab container. Parity iOS demo2 [MainTabView] (Dashboard /
 * Logs / Debug). Tabs are sibling Composables behind a saveable index so
 * recomposition + process death restore the same selection without a
 * nested NavHost.
 *
 * Bottom bar is a **floating pill** (parity iOS 18 native TabView), not a
 * full-width Material3 NavigationBar: wrap-content rounded Surface
 * horizontally centered, with transparent margin around so the pill looks
 * detached from screen edges. Selected tab paints a `PrimaryContainer`
 * rounded background behind icon+label; unselected tabs show flat.
 *
 * Scaffold still wraps the layout so its `innerPadding` reserves the
 * bottomBar's height — content stops above the pill and never gets hidden
 * by it (footer Logout/Delete in MainHub stay reachable).
 *
 * Centers ([SDKEnvironment.toast]/loading/confirm) mount once at root so
 * any screen in the tab graph (or pushed above via outer NavHost) fires
 * without prop-drilling.
 */
@Composable
fun MainTabScreen(
    onProfile: () -> Unit,
    onShop: () -> Unit,
    onBind: () -> Unit,
    onGiftcode: () -> Unit,
    onIdentity: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDebugAntiAddiction: () -> Unit,
    onDebugTokenLog: () -> Unit,
    onDebugRemoteConfig: () -> Unit,
    onAFEvents: () -> Unit,
    onAds: () -> Unit,
    onSignedOut: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = remember {
        listOf(
            TabItem("Dashboard", Icons.Filled.Dashboard),
            TabItem("Logs", Icons.Filled.Receipt),
            TabItem("Debug", Icons.Filled.BugReport),
        )
    }

    // Parity iOS 18 floating TabView: content fills the whole window and
    // scrolls under the pill — pill is overlay-positioned at bottom-center,
    // not a Scaffold bottomBar slot (which would reserve vertical space and
    // hide content beneath). Each tab screen is responsible for its own
    // bottom content padding so the final scroll item clears the pill.
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> MainHubScreen(
                    env = env,
                    onProfile = onProfile,
                    onShop = onShop,
                    onBind = onBind,
                    onGiftcode = onGiftcode,
                    onIdentity = onIdentity,
                    onDeleteAccount = onDeleteAccount,
                    onAntiAddiction = onDebugAntiAddiction,
                    onAFEvents = onAFEvents,
                    onAds = onAds,
                    onSignedOut = onSignedOut,
                )
                1 -> PurchaseHistoryScreen(
                    env = env,
                    onBack = { selectedTab = 0 },
                    mountHosts = false,
                )
                else -> DebugRootScreen(
                    onAntiAddiction = onDebugAntiAddiction,
                    onTokenLog = onDebugTokenLog,
                    onRemoteConfig = onDebugRemoteConfig,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            FloatingTabBar(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
            )
        }

        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

@Composable
private fun FloatingTabBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    // Outer Box: NO background — parity iOS 18 floating TabView where the
    // pill hovers over scrolling content (content visibly passes under both
    // sides of the pill and through the gesture-bar zone). Any scrim here
    // composites against the dark wallpaper and reads as gray/solid on
    // Android (no system blur), defeating the floating effect.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = VLColor.SurfaceContainerLowest,
            tonalElevation = 2.dp,
            shadowElevation = 20.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = VLColor.OutlineVariant,
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabs.forEachIndexed { index, tab ->
                    FloatingTabItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingTabItem(
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) VLColor.Primary else VLColor.OnSurfaceVariant
    val background = if (selected) VLColor.PrimaryContainer else Color.Transparent
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = tint,
        )
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Preview(name = "Phone portrait", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(name = "Phone landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun MainTabScreenPreview() {
    VLTheme {
        MainTabScreen(
            onProfile = {},
            onShop = {},
            onBind = {},
            onGiftcode = {},
            onIdentity = {},
            onDeleteAccount = {},
            onDebugAntiAddiction = {},
            onDebugTokenLog = {},
            onDebugRemoteConfig = {},
            onAFEvents = {},
            onAds = {},
            onSignedOut = {},
        )
    }
}
