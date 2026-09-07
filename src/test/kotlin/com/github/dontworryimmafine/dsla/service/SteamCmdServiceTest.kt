package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.SteamCmdBatchResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SteamCmdServiceTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `continues updating after a game fails and reports each result separately`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.MIXED_RESULTS, appIds = linkedSetOf(10, 20, 30))

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.ERROR, 20L to MessageType.SUCCESS, 30L to MessageType.ALREADY_UP_TO_DATE)
        assertNull(batch.sessionFailure)
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(10L, 20L, 30L), fixture.appIdsWithOutput(MessageType.DOWNLOADING))
    }

    @Test
    fun `preserves completed results and fails unfinished games after a process crash`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.PROCESS_CRASH, appIds = linkedSetOf(10, 20, 30))

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.SUCCESS, 20L to MessageType.ERROR, 30L to MessageType.ERROR)
        val failures = batch.results.values.filterNot { it.resultMessage.type.isSuccessful }
        assertTrue(failures.all { "code 42" in it.resultMessage.message })
    }

    @Test
    fun `does not assign a later games success to a game with no result`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.MISSING_RESULT)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.ERROR, 20L to MessageType.SUCCESS)
        assertEquals(listOf(20L), fixture.appIdsWithOutput(MessageType.DOWNLOADING))
    }

    @Test
    fun `does not treat a SteamCMD self update as a successful game update`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.SELF_UPDATE_ONLY)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.ERROR, 20L to MessageType.ERROR)
    }

    @Test
    fun `validates every game when validation is enabled`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, validate = true)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.SUCCESS, 20L to MessageType.SUCCESS)
        assertEquals(listOf("app_update 10 validate", "app_update 20 validate"), fixture.updateCommands)
        assertEquals(listOf(10L, 20L), fixture.appIdsWithOutput(MessageType.VALIDATING))
    }

    @Test
    fun `keeps app password errors separate from session authentication failures`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.APP_PASSWORD_ERROR)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.ERROR, 20L to MessageType.SUCCESS)
        assertNull(batch.sessionFailure)
        assertEquals(listOf(10L, 20L), batch.results.values.map { it.resultMessage.appId })
        assertEquals(1, fixture.processCount)
    }

    @Test
    fun `updates a large library using one cached login`() {
        // Given
        val appIds = (1L..250L).toSet()
        val fixture = SteamCmdTestFixture(directory, appIds = appIds)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(
            appIds,
            batch.results.keys
                .map { it.appId }
                .toSet(),
        )
        assertTrue(batch.results.values.all { it.resultMessage.type.isSuccessful })
    }

    @Test
    fun `reports authentication failure for every game without starting updates`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.RATE_LIMITED)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps)

        // Then
        assertAppResults(batch, 10L to MessageType.LOGIN_RATE_LIMIT, 20L to MessageType.LOGIN_RATE_LIMIT)
        assertEquals(MessageType.LOGIN_RATE_LIMIT, batch.sessionFailure?.type)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
        assertEquals(emptyList(), fixture.updateCommands)
    }

    @ParameterizedTest
    @ValueSource(strings = ["test-password", "Success", "App"])
    fun `redacts passwords without changing result recognition`(password: String) {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.ECHO_PASSWORD, password = password)

        // When
        val batch = fixture.steamCmdService.updateApps(fixture.apps, usePassword = true)

        // Then
        assertAppResults(batch, 10L to MessageType.SUCCESS, 20L to MessageType.SUCCESS)
        assertFalse(batch.results.values.any { password in it.resultMessage.message })
        assertTrue(fixture.output.any { "<REDACTED>" in it.result.message })
        assertFalse(fixture.output.any { password in it.result.message })
    }

    private fun assertAppResults(
        batch: SteamCmdBatchResult,
        vararg expected: Pair<Long, MessageType>,
    ) {
        val actual = batch.results.values.associate { it.steamApp.appId to it.resultMessage.type }
        assertEquals(expected.toMap(), actual)
    }
}
