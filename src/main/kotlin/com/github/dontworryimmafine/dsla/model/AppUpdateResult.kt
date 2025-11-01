package com.github.dontworryimmafine.dsla.model

data class AppUpdateResult(
    val appId: Long,
    val status: AppUpdateStatus,
    val errorMessage: String? = null,
    val duration: Long = 0
)

enum class AppUpdateStatus {
    SUCCESS,
    FAILED
}