package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.client.SteamApiHttpClient
import com.github.dontworryimmafine.dsla.model.PlayerSummary
import com.github.dontworryimmafine.dsla.model.PlayerSummaryState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SteamApiService(
    private val steamApiHttpClient: SteamApiHttpClient,
) {
    fun isAppsUpdateAllowed(): Boolean {
        val playerSummary = steamApiHttpClient.getPlayerSummary()
        if (playerSummary == null) {
            logger.warn("PlayerSummary is empty. User steam status will be ignored.")
            return true
        }

        return isOfflineOrIdle(playerSummary)
    }

    private fun isOfflineOrIdle(playerSummary: PlayerSummary): Boolean =
        playerSummary.state in
            setOf(
                PlayerSummaryState.UNKNOWN,
                PlayerSummaryState.OFFLINE,
                PlayerSummaryState.SNOOZE,
                PlayerSummaryState.AWAY,
            )

    companion object {
        private val logger = LoggerFactory.getLogger(SteamApiService::class.java)
    }
}
