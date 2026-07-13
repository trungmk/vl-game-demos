package sdk.vlplay.vn.sample.models

import java.util.UUID

/**
 * Token-refresh observability entry shown in the Debug → Token Refresh Log
 * screen. Parity iOS [TokenRefreshLogEntry].
 *
 * @param timestampEpochMillis when the refresh completed (local clock)
 * @param expiresAtEpochSeconds 0 when SDK didn't surface an expiry
 */
data class TokenRefreshLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestampEpochMillis: Long,
    val oldTokenPreview: String,
    val newTokenPreview: String,
    val expiresAtEpochSeconds: Long = 0L,
)
