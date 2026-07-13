package sdk.vlplay.vn.sample.models

import sdk.vlplay.vn.common.UserModel

/**
 * Immutable UI projection of [UserModel]. Parity iOS demo2 reading from
 * [VIDUser]. Hosted in [SDKEnvironment.currentUser] as `StateFlow` so any
 * Compose view re-renders on sign-in / sign-out / token refresh.
 *
 * `UserModel` is a mutable Java POJO — passing it directly to Compose breaks
 * recomposition because mutation doesn't flip equality. We snapshot to this
 * immutable Kotlin model on every state change.
 */
data class UserUiModel(
    val accountId: String,
    val accountName: String,
    val email: String,
    val phone: String,
    val mobile: String,
    val isGuest: Boolean,
    val needUpdateProfile: Boolean,
    val isUpdatePassword: Boolean,
    val createdAt: String,
    val gameVersion: String,
    val userStatus: Int,
    val accessTokenPreview: String,
    val refreshTokenPreview: String,
    val accessTokenLength: Int,
    val refreshTokenLength: Int,
    val expiration: String,
) {
    val displayName: String get() = when {
        accountName.isNotEmpty() -> accountName
        email.isNotEmpty() -> email
        phone.isNotEmpty() -> phone
        mobile.isNotEmpty() -> mobile
        accountId.isNotEmpty() -> "user-${accountId.takeLast(6)}"
        else -> "Khách"
    }

    val hasIdentity: Boolean get() = email.isNotEmpty() || phone.isNotEmpty() || mobile.isNotEmpty()

    companion object {
        /**
         * Snapshot a Java [UserModel]. Returns null when the model has no
         * accountId AND no accessToken — i.e. SDK has nothing cached yet.
         */
        fun fromSdk(user: UserModel?): UserUiModel? {
            if (user == null) return null
            val accountId = user.accountId.orEmpty()
            val token = user.accessToken.orEmpty()
            if (accountId.isEmpty() && token.isEmpty()) return null
            val refresh = user.refreshToken.orEmpty()
            return UserUiModel(
                accountId = accountId,
                accountName = user.accountName.orEmpty(),
                email = user.email,
                phone = user.phone,
                mobile = user.mobile.orEmpty(),
                isGuest = user.isGuest,
                needUpdateProfile = user.isNeedUpdateProfile,
                isUpdatePassword = user.isUpdatePassword,
                createdAt = user.createdAt.orEmpty(),
                gameVersion = user.gameVersion.orEmpty(),
                userStatus = user.userStatus,
                accessTokenPreview = preview(token),
                refreshTokenPreview = preview(refresh),
                accessTokenLength = token.length,
                refreshTokenLength = refresh.length,
                expiration = user.expiration.orEmpty(),
            )
        }

        /** Parity iOS `preview(of:)` helper — first 8 chars + ellipsis + length. */
        fun preview(token: String?): String {
            if (token.isNullOrEmpty()) return "—"
            return "${token.take(8)}…(${token.length} chars)"
        }
    }
}
