package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLDebugBox
import sdk.vlplay.vn.sample.design.components.VLDebugBoxTone
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLToastHost
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
 * Delete-account confirm screen. Parity iOS demo2 [DeleteAccountView]. User
 * must type "DELETE" verbatim before the destructive CTA enables. Wires
 * [VLPlaySDKManager.deactivateAccount]; on success the SDK has already
 * cleared local credentials + signed out, so [onDeactivated] navigates
 * back to the pre-auth Home.
 */
@Composable
fun DeleteAccountScreen(
    onCancel: () -> Unit,
    onDeactivated: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val confirmKeyword = "DELETE"
    var typed by rememberSaveable { mutableStateOf("") }
    val canConfirm = typed == confirmKeyword

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Delete Account")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = VLColor.Error,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(top = VLSpacing.md),
                )

                Text(
                    text = "Delete Account",
                    style = VLFont.h1,
                    color = VLColor.OnSurface,
                )

                Text(
                    text = "This action is permanent. Your session, payment records, " +
                        "and linked providers will be removed.",
                    style = VLFont.bodyMd,
                    color = VLColor.OnSurfaceVariant,
                )

                VLDebugBox(
                    title = "AFFECTED RESOURCES",
                    lines = listOf(
                        "• Active API Keys",
                        "• Staging Environments",
                        "• Historical request logs",
                        "• Linked OAuth providers",
                    ),
                    tone = VLDebugBoxTone.Info,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(VLSpacing.xs),
                ) {
                    Text(
                        text = "Type $confirmKeyword to confirm",
                        style = VLFont.labelMd,
                        color = VLColor.OnSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = {
                            Text(confirmKeyword, style = MaterialTheme.typography.bodyMedium)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrect = false,
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                        ),
                        shape = RoundedCornerShape(VLRadius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VLColor.SurfaceContainerLowest,
                            unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                            focusedBorderColor = VLColor.Error,
                            unfocusedBorderColor = VLColor.Error.copy(alpha = 0.55f),
                        ),
                    )
                }

                VLButton(
                    text = "Deactivate Account",
                    onClick = {
                        confirmDelete(
                            activity = activity,
                            env = env,
                            onSuccess = onDeactivated,
                        )
                    },
                    kind = VLButtonKind.Destructive,
                    enabled = canConfirm,
                )

                VLButton(
                    text = "Cancel",
                    onClick = onCancel,
                    kind = VLButtonKind.Secondary,
                )
            }
        }

        VLToastHost(center = env.toast)
        VLLoadingOverlay(center = env.loading)
        VLConfirmHost(center = env.confirm)
    }
}

private fun confirmDelete(
    activity: Activity?,
    env: SDKEnvironment,
    onSuccess: () -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang xoá tài khoản…")
    VLPlaySDKManager.deactivateAccount(activity, object : VLPlaySDKManager.DeactivateAccountListener {
        override fun onSuccess() {
            env.loading.hide()
            env.toast.success("Đã xóa tài khoản", "Bạn sẽ được đưa về màn hình đăng nhập")
            // SDK cleared creds + signed out internally; sync env snapshot so
            // observers see the empty user state immediately rather than waiting
            // for a UserSignOutListener round-trip (deactivate path bypasses it).
            env.refreshUserSnapshot()
            onSuccess()
        }

        override fun onFailure(message: String?, errorCode: Int) {
            env.loading.hide()
            env.toast.error("Xoá tài khoản thất bại", message ?: "Mã lỗi $errorCode")
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DeleteAccountScreenPreview() {
    VLTheme {
        DeleteAccountScreen(onCancel = {}, onDeactivated = {})
    }
}
