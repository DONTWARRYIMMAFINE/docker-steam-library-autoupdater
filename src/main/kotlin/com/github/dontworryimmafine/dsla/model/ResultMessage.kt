package com.github.dontworryimmafine.dsla.model

data class ResultMessage(
    val message: String,
    val type: MessageType,
)

enum class MessageType {
    INFO,
    SUCCESS,
    ERROR
}
