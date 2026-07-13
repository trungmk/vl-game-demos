package sdk.vlplay.vn.sample.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLPillTone
import sdk.vlplay.vn.sample.design.components.VLStatePill
import sdk.vlplay.vn.sample.design.components.VLToastHost
import sdk.vlplay.vn.sample.design.components.VLTopAppBar
import sdk.vlplay.vn.sample.design.components.VLTopBarVariant
import sdk.vlplay.vn.sample.design.theme.VLColor
import sdk.vlplay.vn.sample.design.theme.VLFont
import sdk.vlplay.vn.sample.design.theme.VLRadius
import sdk.vlplay.vn.sample.design.theme.VLSpacing
import sdk.vlplay.vn.sample.design.theme.VLTheme
import sdk.vlplay.vn.sample.env.SDKEnvironment

/**
 * Linked accounts screen. Parity iOS demo2 [BindSocialView] — informational
 * stub showing 3 provider rows (Facebook / Google / Apple) with bind/unlink
 * buttons.
 *
 * **Scope intentionally narrow**: the underlying SDK API
 * [VLPlaySDKManager.bindProvider] requires a third-party OAuth token, which
 * means each provider needs its own native sign-in flow (FBSDK
 * `LoginManager`, Google `GoogleSignInClient`, Apple ASAuthorization web
 * fallback) before the SDK call is reachable. iOS demo2 also stops at a
 * stub — wire-up belongs to a follow-up that exposes a public OAuth-token
 * helper. Until then, taps surface a "stub" toast.
 *
 * **Cross-platform deviation** from memory `native_option_b_cross_platform_deviation`:
 * Apple Sign-In on Android uses the web OAuth fallback (separate from iOS's
 * native ASAuthorization). The Apple row stays in the UI for visual parity
 * but is never dispatched to the SDK from this screen.
 */
@Composable
fun BindSocialScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Linked Accounts")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Text(
                    text = "Linked Accounts",
                    style = VLFont.h1,
                    color = VLColor.OnSurface,
                )
                Text(
                    text = "Manage your connected social providers. Bind to enable " +
                        "one-tap login or unlink to revoke access.",
                    style = VLFont.bodyMd,
                    color = VLColor.OnSurfaceVariant,
                )

                Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.sm)) {
                    ProviderRow(
                        name = "Facebook",
                        code = "F",
                        brandColor = Color(0xFF1877F2),
                        linked = false,
                        onTap = { env.toast.info("Bind Facebook", "Stub — cần native FBSDK login + bindProvider wire") },
                    )
                    ProviderRow(
                        name = "Google",
                        code = "G",
                        brandColor = VLColor.SurfaceContainerLowest,
                        brandFgOverride = VLColor.OnSurface,
                        outlined = true,
                        linked = true,
                        onTap = { env.toast.warning("Unlink Google", "Stub — cần SDK API + xác nhận") },
                    )
                    ProviderRow(
                        name = "Apple",
                        code = "A",
                        brandColor = Color.Black,
                        linked = false,
                        onTap = { env.toast.info("Bind Apple", "Stub — Android dùng web OAuth fallback (cross-platform deviation)") },
                    )
                }

                VLButton(
                    text = "← Quay lại",
                    onClick = onBack,
                    kind = VLButtonKind.Ghost,
                )
            }
        }

        // Centers — top-level screen mounts its own hosts (parity MainTabScreen).
        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

@Composable
private fun ProviderRow(
    name: String,
    code: String,
    brandColor: Color,
    linked: Boolean,
    onTap: () -> Unit,
    brandFgOverride: Color? = null,
    outlined: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VLRadius.md),
        color = VLColor.SurfaceContainerLowest,
        border = BorderStroke(1.dp, VLColor.OutlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(VLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VLSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(VLRadius.md))
                    .background(brandColor)
                    .then(
                        if (outlined) Modifier else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = code,
                    style = VLFont.h2.copy(fontWeight = FontWeight.Bold),
                    color = brandFgOverride ?: Color.White,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = name,
                    style = VLFont.bodyLg.copy(fontWeight = FontWeight.SemiBold),
                    color = VLColor.OnSurface,
                )
                if (linked) {
                    VLStatePill(text = "LINKED", tone = VLPillTone.Primary)
                } else {
                    Text(
                        text = "Not linked",
                        style = VLFont.bodySm,
                        color = VLColor.OnSurfaceVariant,
                    )
                }
            }

            VLButton(
                text = if (linked) "Unlink" else "Bind",
                onClick = onTap,
                kind = if (linked) VLButtonKind.Secondary else VLButtonKind.Primary,
                fullWidth = false,
                compact = true,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun BindSocialScreenPreview() {
    VLTheme {
        BindSocialScreen(onBack = {})
    }
}
