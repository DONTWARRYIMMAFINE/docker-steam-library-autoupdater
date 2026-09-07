package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.model.PlayerSummaryState
import com.github.dontworryimmafine.dsla.service.strategy.impl.InstalledAppIdResolveStrategy
import com.github.dontworryimmafine.dsla.service.strategy.impl.ManualAppIdResolveStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.assertEquals

internal class AppIdResolutionServiceTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `resolves configured manual app IDs when the library has no manifests`() {
        // Given
        val properties = properties(strategies = setOf(AppIdResolveStrategyType.MANUAL), manualAppIds = setOf(10, 20))
        val service = createService(properties)

        // When
        val appIds = service.resolveAppIds()

        // Then
        assertEquals(setOf(10L, 20L), appIds)
    }

    @Test
    fun `resolves installed app IDs from manifests in the configured library`() {
        // Given
        val steamApps = directory.resolve("steamapps").createDirectories()
        steamApps.resolve("appmanifest_20.acf").writeText("")
        val properties = properties(strategies = setOf(AppIdResolveStrategyType.INSTALLED), manualAppIds = setOf(10))
        val service = createService(properties)

        // When
        val appIds = service.resolveAppIds()

        // Then
        assertEquals(setOf(20L), appIds)
    }

    @Test
    fun `combines manual and installed app IDs and excludes ignored games`() {
        // Given
        val steamApps = directory.resolve("steamapps").createDirectories()
        steamApps.resolve("appmanifest_20.acf").writeText("")
        steamApps.resolve("appmanifest_30.acf").writeText("")
        val properties =
            properties(
                strategies = setOf(AppIdResolveStrategyType.MANUAL, AppIdResolveStrategyType.INSTALLED),
                manualAppIds = setOf(10, 20),
                ignoreAppIds = setOf(30),
            )
        val service = createService(properties)

        // When
        val appIds = service.resolveAppIds()

        // Then
        assertEquals(setOf(10L, 20L), appIds)
    }

    @Test
    fun `does not infer installed app IDs from game directories without manifests`() {
        // Given
        directory.resolve("steamapps/common/Example Game").createDirectories()
        val properties = properties(strategies = setOf(AppIdResolveStrategyType.INSTALLED))
        val service = createService(properties)

        // When
        val appIds = service.resolveAppIds()

        // Then
        assertEquals(emptySet(), appIds)
    }

    private fun createService(properties: SteamProperties): AppIdResolutionService =
        AppIdResolutionService(
            properties,
            mapOf(
                AppIdResolveStrategyType.MANUAL to ManualAppIdResolveStrategy(properties),
                AppIdResolveStrategyType.INSTALLED to InstalledAppIdResolveStrategy(properties),
            ),
        )

    private fun properties(
        strategies: Set<AppIdResolveStrategyType>,
        manualAppIds: Set<Long> = emptySet(),
        ignoreAppIds: Set<Long> = emptySet(),
    ): SteamProperties =
        SteamProperties(
            schedule = "-",
            webApiKey = "",
            steamId = "",
            username = "",
            password = "",
            allowedStates = setOf(PlayerSummaryState.UNKNOWN),
            rootPath = directory.toString(),
            cmdRootPath = directory.resolve("steamcmd").toString(),
            cmdFilterOutput = "true",
            cmdValidateInstalled = false,
            appIdResolveStrategies = strategies,
            manualAppIds = manualAppIds,
            ignoreAppIds = ignoreAppIds,
        )
}
