package com.github.dontworryimmafine.dsla.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Path
import java.time.Duration
import kotlin.test.assertEquals

internal class SteamAppsUpdateServiceTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `updates the resolved games with one cached login`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, appIds = linkedSetOf(10, 20))
        val service = fixture.createUpdater()

        // When
        service.performUpdate()

        // Then
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(listOf("app_update 10", "app_update 20"), fixture.updateCommands)
    }

    @Test
    fun `retries with a password when cached credentials are missing`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.PASSWORD_REQUIRED)
        val service = fixture.createUpdater()

        // When
        service.performUpdate()

        // Then
        assertEquals(2, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS, LoginMethod.PASSWORD), fixture.loginAttempts)
        assertEquals(listOf("app_update 10", "app_update 20"), fixture.updateCommands)
    }

    @Test
    fun `stops retrying when password authentication also reports missing credentials`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.CREDENTIALS_UNAVAILABLE)
        val service = fixture.createUpdater()

        // When
        service.performUpdate()

        // Then
        assertEquals(2, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS, LoginMethod.PASSWORD), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @Test
    fun `does not retry missing credentials when no password is configured`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.CREDENTIALS_UNAVAILABLE, password = "")
        val service = fixture.createUpdater()

        // When
        service.performUpdate()

        // Then
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @ParameterizedTest
    @EnumSource(
        value = SteamCmdScenario::class,
        names = ["RATE_LIMITED", "INVALID_PASSWORD", "STEAM_GUARD_TIMEOUT", "CONNECTION_FAILED"],
    )
    fun `skips scheduled updates while authentication cooldown is active`(scenario: SteamCmdScenario) {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = scenario)
        val clock = MutableClock()
        val service = fixture.createUpdater(clock)
        service.performUpdate()
        fixture.simulate(SteamCmdScenario.SUCCESS)
        clock.advance(Duration.ofMinutes(59))

        // When
        service.performUpdate()

        // Then
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @ParameterizedTest
    @EnumSource(
        value = SteamCmdScenario::class,
        names = ["RATE_LIMITED", "INVALID_PASSWORD", "STEAM_GUARD_TIMEOUT", "CONNECTION_FAILED"],
    )
    fun `resumes scheduled updates when authentication cooldown expires`(scenario: SteamCmdScenario) {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = scenario)
        val clock = MutableClock()
        val service = fixture.createUpdater(clock)
        service.performUpdate()
        fixture.simulate(SteamCmdScenario.SUCCESS)
        clock.advance(Duration.ofHours(1))

        // When
        service.performUpdate()

        // Then
        assertEquals(2, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS, LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(listOf("app_update 10", "app_update 20"), fixture.updateCommands)
    }

    @Test
    fun `pauses subsequent runs after the password retry fails`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.CREDENTIALS_UNAVAILABLE)
        val service = fixture.createUpdater(MutableClock())
        service.performUpdate()

        // When
        service.performUpdate()

        // Then
        assertEquals(2, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS, LoginMethod.PASSWORD), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @Test
    fun `pauses subsequent runs when credentials and password are both missing`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.CREDENTIALS_UNAVAILABLE, password = "")
        val service = fixture.createUpdater(MutableClock())
        service.performUpdate()

        // When
        service.performUpdate()

        // Then
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @Test
    fun `does not start SteamCMD for an empty library`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, appIds = emptySet())
        val service = fixture.createUpdater()

        // When
        service.performUpdate()

        // Then
        assertEquals(0, fixture.processCount)
        assertEquals(emptyList(), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }
}
