package com.github.dontworryimmafine.dsla.acfparser.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppManifest(
    @JsonProperty("AppState")
    val appState: AppState,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppState(
    @JsonProperty("appid")
    val appId: Long,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("installdir")
    val installDir: String,
    @JsonProperty("SizeOnDisk")
    val sizeOnDisk: Long,
    @JsonProperty("LastUpdated")
    val lastUpdated: Long? = null,
    @JsonProperty("buildid")
    val buildId: Long? = null,
    @JsonProperty("StateFlags")
    val stateFlags: String? = null,
    @JsonProperty("WorkshopItems")
    val workshopItems: Map<String, WorkshopItem> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkshopItem(
    @JsonProperty("size")
    val size: Long,
    @JsonProperty("timestamp")
    val timestamp: Long,
)
