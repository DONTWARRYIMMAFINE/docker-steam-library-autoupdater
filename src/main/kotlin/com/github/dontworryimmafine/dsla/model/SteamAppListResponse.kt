package com.github.dontworryimmafine.dsla.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SteamAppListResponse(
    @field:JsonProperty("applist")
    val appList: AppList = AppList(),
)

data class AppList(
    val apps: List<SteamApp> = emptyList(),
)

data class SteamApp(
    @field:JsonProperty("appid")
    val appId: Long,
    val name: String? = null,
) {
    override fun toString() = name ?: appId.toString()
}
