package com.github.dontworryimmafine.dsla.service.strategy.impl

import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.SteamAppManifestService
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ManualAppIdResolveStrategy(
    private val steamAppManifestService: SteamAppManifestService,
) : AppIdResolveStrategy {
    override fun getType(): AppIdResolveStrategyType = AppIdResolveStrategyType.MANUAL

    override fun resolve(): Set<Long> {
        logger.info("Start resolving ids")

        val appIds = steamAppManifestService.loadAppManifests().keys

        logger.info("Resolved ${appIds.size} appIds")
        return appIds
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManualAppIdResolveStrategy::class.java)
    }
}
