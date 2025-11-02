package com.github.dontworryimmafine.dsla.model

data class AppUpdateResult(
    val steamApp: SteamApp,
    val resultMessage: ResultMessage,
    val duration: Long = 0
)
