package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AppIdResolutionService(
    private val properties: SteamProperties,
    private val appIdResolveStrategyMap: Map<AppIdResolveStrategyType, AppIdResolveStrategy>,
) {
    fun resolveAppIds(): Set<Long> =
        with(properties) {
            val resolvedAppIds = appIdResolveStrategies.flatMap { appIdResolveStrategyMap[it]!!.resolve() }
            if (resolvedAppIds.isEmpty()) {
                logger.warn("No appIds resolved using strategies: $appIdResolveStrategies")
                return emptySet()
            }

            val appIds = (resolvedAppIds - ignoreAppIds).toSet()
            if (appIds.isEmpty()) {
                logger.warn("No appIds left after removing ignored")
                return emptySet()
            }

            return appIds
        }

    companion object {
        private val logger = LoggerFactory.getLogger(AppIdResolutionService::class.java)
    }
}
