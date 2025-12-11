package com.github.dontworryimmafine.dsla.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

typealias SteamApiResponse = Map<String, AppDetailsWrapper>

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppDetailsWrapper(
    @JsonProperty("success")
    val success: Boolean,
    @JsonProperty("data")
    val data: AppData? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppData(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("type")
    val type: String? = null,
)

data class SteamApp(
    val appId: Long,
    val name: String? = null,
) {
    override fun toString(): String = name ?: appId.toString()
}
