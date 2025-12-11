package com.github.dontworryimmafine.dsla.client.impl

import com.github.dontworryimmafine.dsla.client.SteamApiHttpClient
import com.github.dontworryimmafine.dsla.config.properties.HttpClientProperties
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.exception.HttpClientException
import com.github.dontworryimmafine.dsla.model.PlayerSummary
import com.github.dontworryimmafine.dsla.model.PlayerSummaryResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class DefaultSteamApiHttpClient(
    restTemplate: RestTemplate,
    private val steamApiHttpClientProperties: HttpClientProperties,
    private val steamProperties: SteamProperties,
) : AbstractHttpClient(restTemplate),
    SteamApiHttpClient {
    override fun getPlayerSummary(): PlayerSummary? {
        if (steamProperties.webApiKey.isBlank() || steamProperties.steamId.isBlank()) {
            logger.warn("STEAM_WEB_API_KEY or STEAM_ID is empty. Unable to determine PlayerSummary")
            return null
        }

        val url = buildUrl()
        return try {
            get(url, PlayerSummaryResponse::class.java)
                .body
                ?.response
                ?.players
                ?.firstOrNull()
                ?: throw HttpClientException("HTTP Client exception. Null body is response: $url ${HttpMethod.GET}")
        } catch (ex: HttpClientException) {
            logger.error(ex.message)
            null
        }
    }

    private fun buildUrl(): String =
        UriComponentsBuilder
            .fromUriString(steamApiHttpClientProperties.baseUrl)
            .path(steamApiHttpClientProperties.getEndpoint(PLAYER_SUMMARY_ENDPOINT))
            .queryParam(API_KEY_QUERY_PARAM, steamProperties.webApiKey)
            .queryParam(STEAM_IDS_QUERY_PARAM, steamProperties.steamId)
            .toUriString()

    companion object {
        private const val PLAYER_SUMMARY_ENDPOINT = "player-summary"
        private const val API_KEY_QUERY_PARAM = "key"
        private const val STEAM_IDS_QUERY_PARAM = "steamids"

        private val logger = LoggerFactory.getLogger(DefaultSteamApiHttpClient::class.java)
    }
}
