package com.github.dontworryimmafine.dsla.client.impl

import com.github.dontworryimmafine.dsla.client.SteamHttpClient
import com.github.dontworryimmafine.dsla.config.properties.HttpClientProperties
import com.github.dontworryimmafine.dsla.exception.HttpClientException
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.model.SteamAppListResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class DefaultSteamHttpClient(
    restTemplate: RestTemplate,
    private val steamHttpClientProperties: HttpClientProperties,
) : AbstractHttpClient(restTemplate),
    SteamHttpClient {
    override fun getAppList(): List<SteamApp> {
        val url =
            UriComponentsBuilder
                .fromUriString(steamHttpClientProperties.baseUrl)
                .path(steamHttpClientProperties.getEndpoint(APP_LIST_ENDPOINT))
                .build()
                .toUriString()

        return try {
            get(url, SteamAppListResponse::class.java).body?.appList?.apps
                ?: throw HttpClientException("HTTP Client exception. Null body is response: $url ${HttpMethod.GET}")
        } catch (ex: HttpClientException) {
            logger.error(ex.message)
            emptyList()
        }
    }

    companion object {
        private const val APP_LIST_ENDPOINT = "app-list"

        private val logger = LoggerFactory.getLogger(DefaultSteamHttpClient::class.java)
    }
}
