package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.client.SteamApiHttpClient
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.PlayerSummaryState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SteamApiService(
    private val steamProperties: SteamProperties,
    private val steamApiHttpClient: SteamApiHttpClient,
) {
    fun hasAllowedToUpdateState(): Boolean {
        val playerSummary =
            steamApiHttpClient.getPlayerSummary()?.state
                ?: PlayerSummaryState.UNKNOWN

        logger.info("Current steam user state: ${playerSummary.name}. Allowed states: ${steamProperties.allowedStates}")
        return steamProperties.allowedStates.isEmpty() || playerSummary in steamProperties.allowedStates
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamApiService::class.java)
    }
}
