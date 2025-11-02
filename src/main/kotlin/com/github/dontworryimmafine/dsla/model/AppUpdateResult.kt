package com.github.dontworryimmafine.dsla.model

data class AppUpdateResult(
    val appId: Long,
    val resultMessage: ResultMessage,
    val duration: Long = 0
)
