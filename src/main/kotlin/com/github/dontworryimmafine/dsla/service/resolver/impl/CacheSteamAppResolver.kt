package com.github.dontworryimmafine.dsla.service.resolver.impl

import com.github.dontworryimmafine.dsla.client.SteamHttpClient
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.resolver.SteamAppResolver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CacheSteamAppResolver(
    private val steamHttpClient: SteamHttpClient
) : SteamAppResolver {
    private val cache = mutableMapOf<Long, SteamApp>()

    override fun resolve(appIds: Set<Long>): List<SteamApp> {
        if (appIds.isEmpty()) {
            logger.warn("No appIds provided. Skipping SteamApp info retrieving")
            return emptyList()
        }

        if (cache.isEmpty()) {
            initializeCache(appIds)
        }

        return appIds.map { appId ->
            val dataFromCache = cache[appId]
            if (dataFromCache == null) {
                logger.warn("Unable to get SteamApp info for: $appId")
                return@map SteamApp(appId)
            }
            dataFromCache
        }
    }

    private fun initializeCache(appIds: Set<Long>) {
        logger.info("Retrieving SteamApps info")
        val steamAppListResponse = steamHttpClient.getAppList()
            .asSequence()
            .filter { it.appId in appIds }
            .associateBy { it.appId }

        val unknownAppIds = appIds - steamAppListResponse.keys
        if (unknownAppIds.isNotEmpty()) {
            logger.error("Unable to get SteamApp info for: $unknownAppIds")
        }

        logger.info("Retrieved SteamApps info for ${steamAppListResponse.size} apps of total ${appIds.size}")
        cache.putAll(steamAppListResponse)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CacheSteamAppResolver::class.java)
    }
}
