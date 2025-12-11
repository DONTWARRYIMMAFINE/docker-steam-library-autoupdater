package com.github.dontworryimmafine.dsla.service.resolver.impl

import com.github.dontworryimmafine.dsla.client.SteamHttpClient
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.resolver.SteamAppResolver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultSteamAppResolver(
    private val steamHttpClient: SteamHttpClient,
) : SteamAppResolver {
    override fun resolve(appIds: Set<Long>): List<SteamApp> {
        if (appIds.isEmpty()) {
            logger.warn("No appIds provided. Skipping SteamApp info retrieving")
            return emptyList()
        }

        return appIds.map { appId -> steamHttpClient.getSteamApp(appId) ?: SteamApp(appId) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSteamAppResolver::class.java)
    }
}
