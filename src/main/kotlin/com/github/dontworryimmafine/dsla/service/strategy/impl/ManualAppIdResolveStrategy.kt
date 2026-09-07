package com.github.dontworryimmafine.dsla.service.strategy.impl

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ManualAppIdResolveStrategy(
    private val properties: SteamProperties,
) : AppIdResolveStrategy {
    override fun getType(): AppIdResolveStrategyType = AppIdResolveStrategyType.MANUAL

    /**
     * Reads the app IDs explicitly configured for manual updates.
     *
     * This strategy does not depend on existing manifests, so it can start downloads into
     * an empty library. Ignored IDs are filtered later by the app ID resolution service.
     *
     * @return The configured manual app IDs, or an empty set when none were supplied.
     */
    override fun resolve(): Set<Long> {
        logger.info("Start resolving ids")

        val appIds = properties.manualAppIds

        logger.info("Resolved ${appIds.size} appIds")
        return appIds
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManualAppIdResolveStrategy::class.java)
    }
}
