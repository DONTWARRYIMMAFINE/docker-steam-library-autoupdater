package com.github.dontworryimmafine.dsla.service.strategy.impl

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ManualAppIdResolveStrategy(
    private val properties: SteamProperties
) : AppIdResolveStrategy {
    override fun getType(): AppIdResolveStrategyType = AppIdResolveStrategyType.MANUAL

    override fun resolve(): Set<Long> {
        logger.info("Start resolving ids")

        val appIds = properties.manualAppIds
        if (appIds.isEmpty()) {
            logger.warn("No appIds were provided. Add them to STEAM_MANUAL_APP_IDS environment variable as comma separated list of appIds.")
            return emptySet()

        }

        logger.info("Resolved ${appIds.size} appIds")
        return appIds
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManualAppIdResolveStrategy::class.java)
    }
}
