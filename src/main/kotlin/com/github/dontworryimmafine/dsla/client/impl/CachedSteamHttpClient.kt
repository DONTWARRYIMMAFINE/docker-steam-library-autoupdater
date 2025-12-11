package com.github.dontworryimmafine.dsla.client.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.dontworryimmafine.dsla.client.SteamHttpClient
import com.github.dontworryimmafine.dsla.config.properties.HttpClientProperties
import com.github.dontworryimmafine.dsla.exception.HttpClientException
import com.github.dontworryimmafine.dsla.model.SteamApiResponse
import com.github.dontworryimmafine.dsla.model.SteamApp
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class CachedSteamHttpClient(
    restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val steamHttpClientProperties: HttpClientProperties,
) : AbstractHttpClient(restTemplate),
    SteamHttpClient {
    @Cacheable(cacheNames = ["steam-apps-data"], unless = "#result == null")
    override fun getSteamApp(appId: Long): SteamApp? {
        val url = buildUrl(appId)
        return try {
            val jsonResponse =
                get(url, String::class.java).body
                    ?: throw HttpClientException("HTTP Client exception. Null body is response: $url ${HttpMethod.GET}")

            val apiResponse: SteamApiResponse = objectMapper.readValue(jsonResponse)
            val wrapper =
                apiResponse[appId.toString()]
                    ?: throw HttpClientException("HTTP Client response don't have $appId data: $url ${HttpMethod.GET}")

            if (!wrapper.success) {
                throw HttpClientException("HTTP Client response is not successful: $url ${HttpMethod.GET}")
            }

            SteamApp(appId, wrapper.data?.name)
        } catch (ex: HttpClientException) {
            logger.error(ex.message)
            null
        }
    }

    private fun buildUrl(appId: Long): String =
        UriComponentsBuilder
            .fromUriString(steamHttpClientProperties.baseUrl)
            .path(steamHttpClientProperties.getEndpoint(APP_DETAILS_ENDPOINT))
            .queryParam(APP_IDS_QUERY_PARAM, appId)
            .toUriString()

    companion object {
        private const val APP_DETAILS_ENDPOINT = "app-details"
        private const val APP_IDS_QUERY_PARAM = "appids"

        private val logger = LoggerFactory.getLogger(CachedSteamHttpClient::class.java)
    }
}
