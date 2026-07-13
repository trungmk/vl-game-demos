package sdk.vlplay.vn.sample.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.sample.design.components.VLPillTone
import sdk.vlplay.vn.sample.design.components.VLPropertyRow
import sdk.vlplay.vn.sample.design.components.VLStatePill
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment
import sdk.vlplay.vn.sample.models.UserUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Read-only profile inspection. Parity iOS demo2 [ProfileView] standardized
 * to openapi-v3.0 in iOS Day 16 (`d68a1db`): 4 cards (Account / Identity-KYC
 * / Authentication / Tokens) + avatar block + refresh bar.
 *
 * **Field availability vs iOS**: Android [UserUiModel] is a thin wrapper
 * around [UserModel] (post-login fields only — `accountId`, `accountName`,
 * `email`, `phone`, `mobile`, `isGuest`, `isUpdatePassword`, token previews,
 * `expiration`). It does NOT carry the raw `loginInfoModel` /
 * `detailAccountInfoModel` Mongo blobs that iOS `VIDUser` exposes, so the
 * Identity-KYC card stays in a "needs `/api/v1/detail/{accountId}` call"
 * state with a placeholder string. Wiring the detail endpoint is a separate
 * SDK task (PARITY tracker — not Phase 6 scope).
 *
 * Refresh button calls [SDKEnvironment.refreshUserSnapshot] which re-reads
 * `VLPlaySDKManager.userModel`. Useful after sign-in so any post-login
 * mutations propagate without depending on a listener.
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val user = env.currentUser
    var lastRefreshedAt by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(VLColor.Surface),
    ) {
        VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Profile")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            AvatarBlock(user)

            RefreshBar(
                lastRefreshedAt = lastRefreshedAt,
                onRefresh = {
                    env.refreshUserSnapshot()
                    lastRefreshedAt = System.currentTimeMillis()
                },
            )

            AccountSection(user)
            IdentitySection()
            AuthenticationSection(user)
            TokensSection(user)

            Spacer(modifier = Modifier.height(VLSpacing.md))

            TextButton(onClick = onBack) {
                Text(
                    text = "← Quay lại",
                    style = VLFont.labelBold,
                    color = VLColor.Primary,
                )
            }
        }
    }
}

@Composable
private fun AvatarBlock(user: UserUiModel?) {
    val handle = user?.accountName?.ifEmpty { null } ?: "user"
    val isGuest = user?.isGuest == true
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VLSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(VLColor.SurfaceContainerLowest),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(VLColor.SurfaceContainerLowest),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials(handle),
                    style = VLFont.h1.copy(fontWeight = FontWeight.Bold),
                    color = VLColor.Primary,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.xs),
        ) {
            Text(
                text = "@$handle",
                style = VLFont.h2,
                color = VLColor.OnSurface,
            )
            VLStatePill(
                text = if (isGuest) "GUEST" else "LINKED",
                tone = if (isGuest) VLPillTone.Tertiary else VLPillTone.Neutral,
            )
        }
        Text(
            text = "Player ID  ${user?.accountId?.takeIf { it.isNotEmpty() } ?: "—"}",
            style = VLFont.code,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun RefreshBar(lastRefreshedAt: Long?, onRefresh: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = VLSpacing.md, vertical = VLSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = VLColor.OnSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = lastRefreshedAt
                    ?.let { "Refreshed ${formatHms(it)}" }
                    ?: "Cached từ login",
                style = VLFont.bodyMd,
                color = VLColor.OnSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = VLColor.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Refresh",
                        style = VLFont.labelBold,
                        color = VLColor.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSection(user: UserUiModel?) {
    Card(title = "Account", subtitle = "data.player từ AuthResponse") {
        VLPropertyRow(
            label = "Player ID (_id)",
            value = user?.accountId?.takeIf { it.isNotEmpty() } ?: "—",
            icon = Icons.Filled.Badge,
            copyable = true,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Username",
            value = user?.accountName?.takeIf { it.isNotEmpty() } ?: "—",
            icon = Icons.Filled.Person,
            copyable = true,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Email",
            value = user?.email?.takeIf { it.isNotEmpty() } ?: "—",
            icon = Icons.Filled.Email,
            copyable = true,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Phone",
            value = listOfNotNull(
                user?.phone?.takeIf { it.isNotEmpty() },
                user?.mobile?.takeIf { it.isNotEmpty() },
            ).firstOrNull() ?: "—",
            icon = Icons.Filled.Phone,
            copyable = true,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Is Guest",
            value = if (user?.isGuest == true) "Yes" else "No",
            icon = Icons.Filled.Person,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Update password required",
            value = if (user?.isUpdatePassword == true) "Yes" else "No",
            icon = Icons.Filled.LockReset,
        )
    }
}

@Composable
private fun IdentitySection() {
    Card(title = "Identity (KYC)", subtitle = "GET /api/v1/detail/{accountId}") {
        Text(
            text = "Detail endpoint chưa được wire trên Android SDK. Endpoint sẵn sàng " +
                "phía iOS (VIDUser.detailAccountInfoModel); Android port là task riêng " +
                "ngoài Phase 6 scope.",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
            modifier = Modifier.padding(vertical = VLSpacing.xs),
        )
    }
}

@Composable
private fun AuthenticationSection(user: UserUiModel?) {
    val signedIn = user != null &&
        user.accessTokenLength > 0 &&
        user.accountId.isNotEmpty()
    Card(title = "Authentication", subtitle = "Auth state + flow") {
        VLPropertyRow(
            label = "Auth type",
            value = if (user?.isGuest == true) "Guest (Quick Start)" else "Linked account",
            icon = Icons.Filled.VerifiedUser,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Signed in",
            value = if (signedIn) "Yes" else "No",
            icon = Icons.Filled.AccountCircle,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Need profile update",
            value = if (user?.needUpdateProfile == true) "Yes" else "No",
            icon = Icons.Filled.LockReset,
        )
    }
}

@Composable
private fun TokensSection(user: UserUiModel?) {
    Card(title = "Tokens", subtitle = "Lưu trữ trong EncryptedSharedPreferences") {
        VLPropertyRow(
            label = "Access token",
            value = user?.accessTokenPreview ?: "—",
            icon = Icons.Filled.Lock,
            copyable = false,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Refresh token",
            value = user?.refreshTokenPreview ?: "—",
            icon = Icons.Filled.Refresh,
            copyable = false,
        )
        SectionDivider()
        VLPropertyRow(
            label = "Expires at",
            value = user?.expiration?.takeIf { it.isNotEmpty() } ?: "—",
            icon = Icons.Filled.Schedule,
        )
    }
}

@Composable
private fun Card(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
    ) {
        Text(
            text = title,
            style = VLFont.labelBold,
            color = VLColor.OnSurfaceVariant,
        )
        subtitle?.let {
            Text(
                text = it,
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(VLRadius.md),
            color = VLColor.SurfaceContainerLowest,
            border = BorderStroke(1.dp, VLColor.OutlineVariant),
        ) {
            Column(modifier = Modifier.padding(VLSpacing.md)) {
                content()
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
        color = VLColor.OutlineVariant.copy(alpha = 0.4f),
    )
}

private fun initials(name: String): String =
    if (name.isBlank()) "?" else name.take(2).uppercase()

private fun formatHms(epochMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))

@Preview(name = "Phone portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Phone landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Preview(name = "Tablet portrait", showBackground = true, widthDp = 600, heightDp = 1024)
@Composable
private fun ProfileScreenPreview() {
    VLTheme {
        ProfileScreen(onBack = {})
    }
}
