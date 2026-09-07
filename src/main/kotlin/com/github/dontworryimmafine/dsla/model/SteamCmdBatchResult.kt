package com.github.dontworryimmafine.dsla.model

data class SteamCmdBatchResult(
    val results: Map<SteamApp, AppUpdateResult>,
    val sessionFailure: ResultMessage? = null,
)
