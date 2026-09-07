package com.github.dontworryimmafine.dsla.model

data class ResultMessage(
    val message: String = "Unknown output",
    val type: MessageType = MessageType.ERROR,
    val appId: Long? = null,
)

enum class MessageType(
    val isSuccessful: Boolean = false,
    val isAuthenticationFailure: Boolean = false,
) {
    // Success
    SUCCESS(isSuccessful = true),
    ALREADY_UP_TO_DATE(isSuccessful = true),

    // Error
    ERROR,
    INCORRECT_PASSWORD(isAuthenticationFailure = true),
    STEAM_GUARD_TIMEOUT(isAuthenticationFailure = true),
    NO_CREDENTIAL_CACHE(isAuthenticationFailure = true),
    LOGIN_RATE_LIMIT(isAuthenticationFailure = true),
    LOGIN_FAILED(isAuthenticationFailure = true),

    // Other
    UPDATE_STARTED,
    DOWNLOADING,
    VALIDATING,
}
