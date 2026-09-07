package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.SteamCmdBatchResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

internal class SteamCmdLifecycleTest {
    @TempDir
    lateinit var directory: Path

    private val context = AnnotationConfigApplicationContext()
    private val executor = Executors.newSingleThreadExecutor()

    @AfterEach
    fun closeResources() {
        try {
            context.close()
        } finally {
            executor.shutdownNow()
            check(executor.awaitTermination(5, TimeUnit.SECONDS)) { "SteamCMD test worker did not stop" }
        }
    }

    @Test
    fun `rejects a second update while a session is running`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.RUNNING_UPDATE)
        startUpdate(fixture)

        // When
        val exception = assertFailsWith<IllegalStateException> { fixture.steamCmdService.updateApps(fixture.apps) }

        // Then
        assertEquals("A SteamCMD session is already running", exception.message)
        assertEquals(1, fixture.processCount)
        assertEquals(listOf(LoginMethod.CACHED_CREDENTIALS), fixture.loginAttempts)
    }

    @Test
    fun `stops an active SteamCMD process when the application shuts down`() {
        // Given
        val fixture = SteamCmdTestFixture(directory, scenario = SteamCmdScenario.RUNNING_UPDATE)
        val update = startUpdate(fixture)

        // When
        context.close()
        val batch = update.get(5, TimeUnit.SECONDS)

        // Then
        assertFalse(fixture.isProcessAlive)
        assertEquals(1, fixture.processCount)
        assertEquals(fixture.apps.toSet(), batch.results.keys)
        assertFalse(batch.results.values.any { it.resultMessage.type.isSuccessful })
    }

    @Test
    fun `rejects updates after application shutdown`() {
        // Given
        val fixture = SteamCmdTestFixture(directory)
        registerService(fixture)
        context.close()

        // When
        val exception = assertFailsWith<IllegalStateException> { fixture.steamCmdService.updateApps(fixture.apps) }

        // Then
        assertEquals("SteamCMD service is shutting down", exception.message)
        assertEquals(0, fixture.processCount)
        assertEquals(emptyList(), fixture.loginAttempts)
    }

    private fun startUpdate(fixture: SteamCmdTestFixture): Future<SteamCmdBatchResult> {
        registerService(fixture)
        val update = executor.submit<SteamCmdBatchResult> { fixture.steamCmdService.updateApps(fixture.apps) }
        fixture.awaitDownloadStarted()
        return update
    }

    private fun registerService(fixture: SteamCmdTestFixture) {
        context.registerBean(SteamCmdService::class.java, Supplier { fixture.steamCmdService })
        context.refresh()
    }
}
