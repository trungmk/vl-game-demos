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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import sdk.vlplay.vn.config.SdkConfig
import sdk.vlplay.vn.sample.design.components.VLButton
import sdk.vlplay.vn.sample.design.components.VLButtonKind
import sdk.vlplay.vn.sample.design.components.VLConfirmHost
import sdk.vlplay.vn.sample.design.components.VLLoadingOverlay
import sdk.vlplay.vn.sample.design.components.VLOTPInput
import sdk.vlplay.vn.sample.design.components.VLPropertyRow
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
 * CCCD identity-verification screen (Decree 147/2024). Parity iOS demo2
 * [IdentityVerificationView]. 3-step flow:
 *
 * 1. **Form**: collect họ tên + CCCD (12 digits) + phone (only if CMS
 *    `otpRequired` is ON).
 * 2. **OTP** (only if `otpRequired`): real SDK [VLPlaySDKManager.sendOTP] →
 *    OTP entry → [VLPlaySDKManager.verifyOTP].
 * 3. **Done**: success state.
 *
 * **Known gap (parity iOS)**: the BE persistence endpoint
 * `POST /api/v1/anti-addiction/identity-verification` is wired in the SDK
 * via [VLPlaySDKManager.submitIdentityVerification], but it requires a
 * `dob` field which iOS demo2 doesn't collect. To stay UX-parity with
 * iOS, the demo also doesn't call submit yet — Decree 147 persistence is
 * a follow-up that adds a DOB picker + the submit call. Until then, OTP
 * success is treated as "verified" without persisting CCCD payload.
 */
@Composable
fun IdentityVerificationScreen(
    onBack: () -> Unit,
    env: SDKEnvironment = viewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val otpRequired = remember {
        VLPlaySDKManager.isFeatureEnabled(SdkConfig.FEATURE_OTP_REQUIRED)
    }

    var step by rememberSaveable { mutableStateOf(IdentityStep.Form.name) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var cccd by rememberSaveable { mutableStateOf("") }
    var countryCode by rememberSaveable { mutableStateOf("+84") }
    var phone by rememberSaveable { mutableStateOf("") }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var resendCountdown by rememberSaveable { mutableStateOf(0) }

    // Resend timer — ticks down to 0 once started, idle when at 0.
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000L)
            resendCountdown -= 1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(VLColor.Surface),
        ) {
            VLTopAppBar(variant = VLTopBarVariant.Brand, subtitle = "Xác minh danh tính")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VLSpacing.safeMargin, vertical = VLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
            ) {
                Text(
                    text = "Xác minh CCCD",
                    style = VLFont.h1,
                    color = VLColor.OnSurface,
                )
                Text(
                    text = if (otpRequired) {
                        "Điền thông tin và xác thực số điện thoại qua OTP."
                    } else {
                        "Điền thông tin để xác minh tài khoản."
                    },
                    style = VLFont.bodyMd,
                    color = VLColor.OnSurfaceVariant,
                )

                when (IdentityStep.valueOf(step)) {
                    IdentityStep.Form -> FormStep(
                        otpRequired = otpRequired,
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        cccd = cccd,
                        onCccdChange = { if (it.length <= 12 && it.all(Char::isDigit)) cccd = it },
                        countryCode = countryCode,
                        onCountryCodeChange = { countryCode = it },
                        phone = phone,
                        onPhoneChange = { if (it.length <= 10 && it.all(Char::isDigit)) phone = it },
                        onSubmit = {
                            if (otpRequired) {
                                sendOtp(
                                    activity = activity,
                                    env = env,
                                    phone = phone,
                                    onSuccess = {
                                        env.toast.success("OTP đã gửi", "đến $countryCode$phone")
                                        step = IdentityStep.Otp.name
                                        resendCountdown = 60
                                    },
                                )
                            } else {
                                step = IdentityStep.Done.name
                                env.toast.success("Đã ghi nhận", "Thông tin CCCD đã lưu (demo)")
                            }
                        },
                    )

                    IdentityStep.Otp -> OtpStep(
                        fullName = fullName,
                        cccd = cccd,
                        countryCode = countryCode,
                        phone = phone,
                        otpCode = otpCode,
                        onOtpChange = { if (it.length <= 6 && it.all(Char::isDigit)) otpCode = it },
                        resendCountdown = resendCountdown,
                        onResend = {
                            sendOtp(
                                activity = activity,
                                env = env,
                                phone = phone,
                                onSuccess = {
                                    env.toast.success("OTP đã gửi lại", "đến $countryCode$phone")
                                    resendCountdown = 60
                                },
                            )
                        },
                        onVerify = {
                            verifyOtp(
                                activity = activity,
                                env = env,
                                phone = phone,
                                otpCode = otpCode,
                                onSuccess = {
                                    env.toast.success("Xác thực thành công", "CCCD đã được ghi nhận (demo)")
                                    step = IdentityStep.Done.name
                                },
                                onFailure = { otpCode = "" },
                            )
                        },
                        onBackToForm = {
                            step = IdentityStep.Form.name
                            otpCode = ""
                            resendCountdown = 0
                        },
                    )

                    IdentityStep.Done -> DoneStep(
                        otpRequired = otpRequired,
                        fullName = fullName,
                        cccd = cccd,
                        countryCode = countryCode,
                        phone = phone,
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

private enum class IdentityStep { Form, Otp, Done }

@Composable
private fun FormStep(
    otpRequired: Boolean,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    cccd: String,
    onCccdChange: (String) -> Unit,
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val baseValid = fullName.trim().isNotEmpty() && cccd.length == 12
    val phoneValid = phone.length == 10
    val canSubmit = if (otpRequired) baseValid && phoneValid else baseValid

    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.md)) {
        FieldBlock(
            title = "Họ và tên",
            placeholder = "Nguyễn Văn A",
            value = fullName,
            onValueChange = onFullNameChange,
            keyboardType = KeyboardType.Text,
        )
        FieldBlock(
            title = "Số CCCD (12 chữ số)",
            placeholder = "012345678901",
            value = cccd,
            onValueChange = onCccdChange,
            keyboardType = KeyboardType.Number,
        )

        if (otpRequired) {
            Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.xs)) {
                Text(
                    text = "Số điện thoại",
                    style = VLFont.labelMd,
                    color = VLColor.OnSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VLSpacing.sm),
                ) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = onCountryCodeChange,
                        modifier = Modifier
                            .width(86.dp)
                            .height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(VLRadius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VLColor.SurfaceContainerLowest,
                            unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                        ),
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = { Text("0123456789", style = MaterialTheme.typography.bodyMedium) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(VLRadius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VLColor.SurfaceContainerLowest,
                            unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                            focusedBorderColor = VLColor.Primary,
                            unfocusedBorderColor = VLColor.OutlineVariant,
                        ),
                    )
                }
            }
        }

        VLButton(
            text = if (otpRequired) "Gửi mã OTP" else "Xác minh ngay",
            onClick = onSubmit,
            kind = VLButtonKind.Primary,
            enabled = canSubmit,
        )
    }
}

@Composable
private fun OtpStep(
    fullName: String,
    cccd: String,
    countryCode: String,
    phone: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    resendCountdown: Int,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    onBackToForm: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.md)) {
        VLPropertyRow(label = "Họ tên", value = fullName, icon = Icons.Filled.Person)
        VLPropertyRow(label = "CCCD", value = cccd, icon = Icons.Filled.Badge)
        VLPropertyRow(label = "Điện thoại", value = "$countryCode$phone", icon = Icons.Filled.Phone)

        HorizontalDivider(color = VLColor.OutlineVariant.copy(alpha = 0.4f))

        Text(
            text = "Nhập mã OTP đã gửi đến $countryCode$phone",
            style = VLFont.bodyMd,
            color = VLColor.OnSurfaceVariant,
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            VLOTPInput(code = otpCode, onCodeChange = onOtpChange)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (resendCountdown > 0) {
                Text(
                    text = "Gửi lại sau ${resendCountdown}s",
                    style = VLFont.bodySm,
                    color = VLColor.OnSurfaceVariant,
                )
            } else {
                VLButton(
                    text = "Gửi lại OTP",
                    onClick = onResend,
                    kind = VLButtonKind.Ghost,
                    fullWidth = false,
                    compact = true,
                )
            }
        }

        VLButton(
            text = "Xác thực & hoàn tất",
            onClick = onVerify,
            kind = VLButtonKind.Primary,
            enabled = otpCode.length >= 6,
        )

        VLButton(
            text = "Quay lại sửa thông tin",
            onClick = onBackToForm,
            kind = VLButtonKind.Ghost,
        )
    }
}

@Composable
private fun DoneStep(
    otpRequired: Boolean,
    fullName: String,
    cccd: String,
    countryCode: String,
    phone: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VLSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VLColor.Success,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = "Xác minh thành công",
            style = VLFont.h1,
            color = VLColor.OnSurface,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(VLRadius.md),
            color = VLColor.SurfaceContainerLowest,
        ) {
            Column(modifier = Modifier.padding(VLSpacing.md)) {
                VLPropertyRow(label = "Họ tên", value = fullName, icon = Icons.Filled.Person)
                VLPropertyRow(label = "CCCD", value = cccd, icon = Icons.Filled.Badge)
                if (otpRequired) {
                    VLPropertyRow(
                        label = "Điện thoại",
                        value = "$countryCode$phone",
                        icon = Icons.Filled.Phone,
                    )
                }
            }
        }
        Text(
            text = "Note: chưa wire submitIdentityVerification (cần thêm DOB field) — " +
                "demo chỉ xác thực OTP. Phase 7+ follow-up sẽ persist Decree 147 payload.",
            style = VLFont.bodySm,
            color = VLColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun FieldBlock(
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
) {
    Column(verticalArrangement = Arrangement.spacedBy(VLSpacing.xs)) {
        Text(
            text = title,
            style = VLFont.labelMd,
            color = VLColor.OnSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(VLRadius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VLColor.SurfaceContainerLowest,
                unfocusedContainerColor = VLColor.SurfaceContainerLowest,
                focusedBorderColor = VLColor.Primary,
                unfocusedBorderColor = VLColor.OutlineVariant,
            ),
        )
    }
}

private fun sendOtp(
    activity: Activity?,
    env: SDKEnvironment,
    phone: String,
    onSuccess: () -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang gửi OTP…")
    VLPlaySDKManager.sendOTP(activity, phone, object : VLPlaySDKManager.OTPRequestListener {
        override fun onSuccess() {
            env.loading.hide()
            onSuccess()
        }

        override fun onFailure(message: String?, errorCode: Int) {
            env.loading.hide()
            env.toast.error("Lỗi gửi OTP", message ?: "Mã lỗi $errorCode")
        }
    })
}

private fun verifyOtp(
    activity: Activity?,
    env: SDKEnvironment,
    phone: String,
    otpCode: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
) {
    if (activity == null) {
        env.toast.error("Lỗi", "Activity không khả dụng")
        return
    }
    env.loading.show("Đang xác thực…")
    VLPlaySDKManager.verifyOTP(activity, phone, otpCode, object : VLPlaySDKManager.OTPVerifyListener {
        override fun onSuccess() {
            env.loading.hide()
            onSuccess()
        }

        override fun onFailure(message: String?, errorCode: Int) {
            env.loading.hide()
            env.toast.error("Lỗi xác thực", message ?: "Mã lỗi $errorCode")
            onFailure()
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
private fun IdentityVerificationScreenPreview() {
    VLTheme {
        IdentityVerificationScreen(onBack = {})
    }
}
