package com.github.dontworryimmafine.dsla.model

data class ResultMessage(
    val message: String = "Unknown output",
    val type: MessageType = MessageType.ERROR,
)

enum class MessageType {
    // Success
    SUCCESS,
    ALREADY_UP_TO_DATE,

    // Error
    ERROR,
    INCORRECT_PASSWORD,
    STEAM_GUARD_TIMEOUT,
    NO_CREDENTIAL_CACHE,

    // Other
    DOWNLOADING,
    VALIDATING,
}
