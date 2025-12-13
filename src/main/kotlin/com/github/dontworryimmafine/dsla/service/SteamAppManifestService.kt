package com.github.dontworryimmafine.dsla.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dontworryimmafine.dsla.acfparser.AcfParser
import com.github.dontworryimmafine.dsla.acfparser.dto.AppManifest
import com.github.dontworryimmafine.dsla.acfparser.dto.AppState
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class SteamAppManifestService(
    private val properties: SteamProperties,
    private val objectMapper: ObjectMapper,
) {
    fun loadAppManifests(): Map<Long, AppState> {
        logger.info("Starting load app manifests")
        val steamAppsDir = File("${properties.rootPath}/steamapps")
        if (!steamAppsDir.exists()) {
            logger.warn("Steam apps directory does not exist: ${steamAppsDir.absolutePath}")
            return emptyMap()
        }

        val appManifests =
            steamAppsDir.listFiles { file ->
                file.name.matches(Regex("appmanifest_\\d+\\.acf"))
            }

        if (appManifests.isNullOrEmpty()) {
            logger.warn("Unable to find any app manifests at: ${steamAppsDir.absolutePath}")
            return emptyMap()
        }

        return appManifests
            .mapNotNull { parseManifestOrNull(it) }
            .associate { it.appState.appId to it.appState }
    }

    private fun parseManifestOrNull(file: File): AppManifest? =
        try {
            val manifestAsMap = AcfParser.parseFile(file).asMap()
            objectMapper.convertValue(manifestAsMap, AppManifest::class.java)
        } catch (ex: Exception) {
            logger.warn("Unable to parse file: ${file.absolutePath}. Reason: ${ex.message}")
            null
        }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamAppManifestService::class.java)
    }
}
