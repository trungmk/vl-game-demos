package sdk.vlplay.vn.sample.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import sdk.vlplay.vn.tracking.VLPlaySDKManager

/**
 * Upgrade-guest screen. Parity iOS demo2 [UpgradeGuestView]. Promotes the
 * current Quick-Start session into a real account by setting username +
 * password (+ optional email) — game progress persists, only the auth shape
 * changes.
 *
 * Validation matches BE Joi (openapi-v3.0 GuestUpgradeRequest):
 * - Username: 8-12 chars, lowercase alphanumeric only — regex `^[a-z0-9]{8,12}$`
 * - Password: 8-255 chars
 * - Confirm-password must match
 * - Email optional; trimmed before submit
 *
 * Wires [VLPlaySDKManager.upgradeGuestAccount]. On success the SDK rotates
 * tokens + flips `userModel.isGuest=false` and our env listener already
 * surfaces a "signed in" toast; we add a context-specific success toast +
 * pop back so the hub re-renders the LINKED pill.
 */
@Composable
fun UpgradeGuestScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    val usernameValid = isUsernameValid(username)
    val passwordValid = password.length in 8..255
    val confirmMatches = password == confirmPassword
    val canSubmit = usernameValid && passwordValid && confirmMatches

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Upgrade Account")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
                ) {
                    Text(
                        text = "Nâng cấp tài khoản",
                        style = VLFont.h1,
                        color = VLColor.OnSurface,
                    )
                    VLStatePill(text = "GUEST", tone = VLPillTone.Tertiary)
                }

                Text(
                    text = "Đặt username + password cho phiên Guest hiện tại. Tiến trình " +
                        "chơi được giữ nguyên.",
                    style = VLFont.bodyMd,
                    color = VLColor.OnSurfaceVariant,
                )

                LabeledField(
                    icon = Icons.Filled.Person,
                    label = "Username",
                    hint = "8–12 ký tự, chỉ chữ thường và số",
                    value = username,
                    onValueChange = { username = it },
                    keyboardType = KeyboardType.Ascii,
                    secure = false,
                )
                LabeledField(
                    icon = Icons.Filled.Lock,
                    label = "Password",
                    hint = "Tối thiểu 8 ký tự",
                    value = password,
                    onValueChange = { password = it },
                    keyboardType = KeyboardType.Password,
                    secure = true,
                )
                LabeledField(
                    icon = Icons.Filled.LockReset,
                    label = "Confirm Password",
                    hint = null,
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    keyboardType = KeyboardType.Password,
                    secure = true,
                )
                LabeledField(
                    icon = Icons.Filled.Email,
                    label = "Email (tuỳ chọn)",
                    hint = null,
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email,
                    secure = false,
                )

                VLButton(
                    text = "Nâng cấp tài khoản  →",
                    onClick = {
                        submit(
                            activity = activity,
                            env = env,
                            username = username,
                            password = password,
                            email = email.trim(),
                            onDone = onBack,
                        )
                    },
                    kind = VLButtonKind.Primary,
                    enabled = canSubmit,
                )

                VLButton(
                    text = "Huỷ",
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

private fun submit(
    activity: Activity?,
    env: SDKEnvironment,
    username: String,
    password: String,
    email: String,
    onDone: () -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang nâng cấp…")
    VLPlaySDKManager.upgradeGuestAccount(
        activity, username, password, email.ifEmpty { null },
        object : VLPlaySDKManager.GuestUpgradeListener {
            override fun onSuccess(responseData: org.json.JSONObject) {
                env.loading.hide()
                env.toast.success("Upgrade thành công", "Guest đã thành tài khoản @$username")
                env.refreshUserSnapshot()
                onDone()
            }

            override fun onFailure(message: String?, errorCode: Int) {
                env.loading.hide()
                env.toast.error("Upgrade thất bại", message ?: "Mã lỗi $errorCode")
            }
        },
    )
}

@Composable
private fun LabeledField(
    icon: ImageVector,
    label: String,
    hint: String?,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    secure: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.xs)) {
        Text(
            text = label,
            style = VLFont.labelMd,
            color = VLColor.OnSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VLColor.OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (secure) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            shape = RoundedCornerShape(VLRadius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VLColor.SurfaceContainerLowest,
                unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                focusedBorderColor = VLColor.Primary,
                unfocusedBorderColor = VLColor.OutlineVariant,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        hint?.let {
            Text(
                text = it,
                style = VLFont.bodySm,
                color = VLColor.OnSurfaceVariant,
            )
        }
    }
}

private fun isUsernameValid(name: String): Boolean =
    name.matches(Regex("^[a-z0-9]{8,12}$"))

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
private fun UpgradeGuestScreenPreview() {
    VLTheme {
        UpgradeGuestScreen(onBack = {})
    }
}
