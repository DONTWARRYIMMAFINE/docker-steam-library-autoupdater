package com.github.dontworryimmafine.dsla.service.strategy.impl

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class InstalledAppIdResolveStrategy(
    private val properties: SteamProperties,
) : AppIdResolveStrategy {
    override fun getType(): AppIdResolveStrategyType = AppIdResolveStrategyType.INSTALLED

    override fun resolve(): Set<Long> {
        logger.info("Start resolving ids")

        val steamAppsDir = File("${properties.rootPath}/steamapps")
        if (!steamAppsDir.exists()) {
            logger.warn("Steam apps directory does not exist: ${steamAppsDir.absolutePath}")
            return emptySet()
        }

        val appManifests =
            steamAppsDir.listFiles { file ->
                file.name.matches(Regex("appmanifest_\\d+\\.acf"))
            }

        if (appManifests.isNullOrEmpty()) {
            logger.warn("No app manifest file exists: ${steamAppsDir.absolutePath}")
            return emptySet()
        }

        val appIds =
            appManifests
                .map { file ->
                    file.name
                        .removePrefix("appmanifest_")
                        .removeSuffix(".acf")
                        .toLong()
                }.toSet()

        logger.info("Resolved ${appIds.size} appIds")
        return appIds
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InstalledAppIdResolveStrategy::class.java)
    }
}
