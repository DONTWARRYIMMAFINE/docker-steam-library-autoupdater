package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SteamCmdBatchTrackerTest {
    @Test
    fun `keeps the elapsed time when a start message repeats for the current app`() {
        // Given
        val app = SteamApp(10)
        var elapsedSeconds = 0L
        val batch = SteamCmdBatchTracker(listOf(app)) { TimeUnit.SECONDS.toNanos(elapsedSeconds) }
        val started = ResultMessage(type = MessageType.UPDATE_STARTED, appId = app.appId)
        batch.record(started)
        elapsedSeconds = 3

        // When
        batch.record(started)
        elapsedSeconds = 5
        batch.record(ResultMessage(type = MessageType.SUCCESS, appId = app.appId))
        val result = batch.toResult(exitCode = 0).results.getValue(app)

        // Then
        assertEquals(5_000L, result.duration)
    }

    @Test
    fun `measures the next apps duration from the end of the previous update`() {
        // Given
        val firstApp = SteamApp(10)
        val secondApp = SteamApp(20)
        var elapsedSeconds = 0L
        val batch = SteamCmdBatchTracker(listOf(firstApp, secondApp)) { TimeUnit.SECONDS.toNanos(elapsedSeconds) }
        elapsedSeconds = 2
        batch.record(ResultMessage(type = MessageType.SUCCESS, appId = firstApp.appId))

        // When
        elapsedSeconds = 7
        val completedApp = batch.record(ResultMessage(type = MessageType.SUCCESS, appId = secondApp.appId))
        val activeApp = batch.record(ResultMessage(type = MessageType.DOWNLOADING))
        val results = batch.toResult(exitCode = 0).results

        // Then
        assertEquals(secondApp, completedApp)
        assertEquals(2_000L, results.getValue(firstApp).duration)
        assertEquals(5_000L, results.getValue(secondApp).duration)
        assertNull(activeApp)
    }

    @Test
    fun `ignores unknown app messages without changing the active app`() {
        // Given
        val app = SteamApp(10)
        val batch = SteamCmdBatchTracker(listOf(app))
        val unknownAppMessage = ResultMessage(type = MessageType.SUCCESS, appId = 999)

        // When
        val unknownApp = batch.record(unknownAppMessage)
        val activeApp = batch.record(ResultMessage(type = MessageType.DOWNLOADING))
        val result = batch.toResult(exitCode = 0).results.getValue(app)

        // Then
        assertNull(unknownApp)
        assertEquals(app, activeApp)
        assertEquals(MessageType.ERROR, result.resultMessage.type)
    }

    @Test
    fun `starts timing a later app when the previous app has no result`() {
        // Given
        val firstApp = SteamApp(10)
        val secondApp = SteamApp(20)
        var elapsedSeconds = 0L
        val batch = SteamCmdBatchTracker(listOf(firstApp, secondApp)) { TimeUnit.SECONDS.toNanos(elapsedSeconds) }

        // When
        elapsedSeconds = 3
        batch.record(ResultMessage(type = MessageType.UPDATE_STARTED, appId = secondApp.appId))
        val activeApp = batch.record(ResultMessage(type = MessageType.DOWNLOADING))
        elapsedSeconds = 7
        batch.record(ResultMessage(type = MessageType.SUCCESS, appId = secondApp.appId))
        val results = batch.toResult(exitCode = 0).results

        // Then
        assertEquals(secondApp, activeApp)
        assertEquals(MessageType.ERROR, results.getValue(firstApp).resultMessage.type)
        assertEquals(MessageType.SUCCESS, results.getValue(secondApp).resultMessage.type)
        assertEquals(4_000L, results.getValue(secondApp).duration)
    }

    @Test
    fun `ignores repeated completion messages without overwriting results or restarting the next app`() {
        // Given
        val firstApp = SteamApp(10)
        val secondApp = SteamApp(20)
        var elapsedSeconds = 0L
        val batch = SteamCmdBatchTracker(listOf(firstApp, secondApp)) { TimeUnit.SECONDS.toNanos(elapsedSeconds) }
        val success = ResultMessage(type = MessageType.SUCCESS, appId = firstApp.appId)
        elapsedSeconds = 2
        batch.record(success)

        // When
        elapsedSeconds = 5
        val repeatedApp = batch.record(success)
        val activeApp = batch.record(ResultMessage(type = MessageType.DOWNLOADING))
        elapsedSeconds = 7
        batch.record(ResultMessage(type = MessageType.SUCCESS, appId = secondApp.appId))
        val results = batch.toResult(exitCode = 0).results

        // Then
        assertNull(repeatedApp)
        assertEquals(secondApp, activeApp)
        assertEquals(2_000L, results.getValue(firstApp).duration)
        assertEquals(5_000L, results.getValue(secondApp).duration)
    }

    @Test
    fun `returns no active app or results for an empty batch`() {
        // Given
        val batch = SteamCmdBatchTracker(emptyList())

        // When
        val activeApp = batch.record(ResultMessage(type = MessageType.DOWNLOADING))
        val result = batch.toResult(exitCode = 0)

        // Then
        assertNull(activeApp)
        assertEquals(emptyMap(), result.results)
        assertNull(result.sessionFailure)
    }

    @Test
    fun `preserves completed updates when authentication fails during the batch`() {
        // Given
        val firstApp = SteamApp(10)
        val secondApp = SteamApp(20)
        val batch = SteamCmdBatchTracker(listOf(firstApp, secondApp))
        val success = ResultMessage(type = MessageType.SUCCESS, appId = firstApp.appId)
        val failure = ResultMessage(message = "Rate limit exceeded", type = MessageType.LOGIN_RATE_LIMIT)
        batch.record(success)

        // When
        val activeApp = batch.record(failure)
        val result = batch.toResult(exitCode = 5)

        // Then
        assertEquals(secondApp, activeApp)
        assertEquals(failure, result.sessionFailure)
        assertEquals(success, result.results.getValue(firstApp).resultMessage)
        assertEquals(failure, result.results.getValue(secondApp).resultMessage)
    }
}
