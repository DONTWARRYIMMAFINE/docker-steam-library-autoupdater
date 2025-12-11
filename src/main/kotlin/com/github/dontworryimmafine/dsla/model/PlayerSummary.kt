package com.github.dontworryimmafine.dsla.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerSummaryResponse(
    @JsonProperty("response")
    val response: PlayerSummaryContainer?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerSummaryContainer(
    @JsonProperty("players")
    val players: List<PlayerSummary> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerSummary(
    @JsonProperty("steamid")
    val steamId: String,
    @JsonProperty("personaname")
    val name: String,
    @JsonProperty("personastate")
    val state: PlayerSummaryState,
)

enum class PlayerSummaryState(
    @JsonValue
    val code: Int,
) {
    OFFLINE(0),
    ONLINE(1),
    BUSY(2),
    AWAY(3),
    SNOOZE(4),
    LOOKING_TO_TRADE(5),
    LOOKING_TO_PLAY(6),
    UNKNOWN(-1),
    ;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromCode(code: Int): PlayerSummaryState = entries.find { it.code == code } ?: UNKNOWN
    }
}
