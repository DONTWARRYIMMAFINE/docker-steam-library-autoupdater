package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.model.SteamCmdBatchResult
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class SteamCmdService(
    private val properties: SteamProperties,
    private val consumers: List<SteamCmdOutputConsumer>,
    private val defaultOutputHandler: OutputHandler,
    private val steamCmdCommandService: SteamCmdCommandService,
) {
    private val processLock = Any()
    private var activeProcess: Process? = null
    private var shuttingDown = false

    /**
     * Updates all requested apps in one SteamCMD session and collects their results.
     *
     * Authentication failures terminate the session; individual app failures allow later
     * updates to continue. Process cleanup runs after completion or failure. This method
     * performs a single login attempt and leaves retry decisions to the caller.
     *
     * @param steamApps Apps with distinct positive IDs, in update order; an empty list returns
     * an empty result without starting a process.
     * @param usePassword Whether to supply the configured password instead of using cached
     * credentials alone; defaults to false.
     * @return A result for every requested app, plus any session authentication failure.
     * Apps without an explicit completion receive a failure result.
     * @throws IllegalArgumentException If an app ID is not positive.
     * @throws IllegalStateException If a session is already running or shutdown has begun.
     * @throws java.io.IOException If the process cannot start or its output cannot be read.
     * @throws InterruptedException If waiting for the process is interrupted; the interrupt flag is restored.
     */
    fun updateApps(
        steamApps: List<SteamApp>,
        usePassword: Boolean = false,
    ): SteamCmdBatchResult {
        if (steamApps.isEmpty()) return SteamCmdBatchResult(emptyMap())

        val command = steamCmdCommandService.buildUpdateCommand(steamApps.map { it.appId }, usePassword)
        val batch = SteamCmdBatchTracker(steamApps)
        val process = startProcess(command)
        logger.info("Started one SteamCMD session for ${steamApps.size} applications")

        try {
            consumeOutput(process, batch)
            val exitCode = process.waitFor()
            if (exitCode != 0) logger.warn("SteamCMD exited with code $exitCode")
            return batch.toResult(exitCode)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ex
        } finally {
            terminate(process)
            synchronized(processLock) { activeProcess = null }
        }
    }

    /**
     * Starts and registers the active SteamCMD process while holding the process lock.
     * Uses the configured SteamCMD directory and merges standard error into standard output.
     *
     * @param command Executable path and individual arguments, as built by [SteamCmdCommandService].
     * @return The started process, registered for shutdown and concurrent-session checks.
     * @throws IllegalStateException If another session is active or shutdown has begun.
     * @throws java.io.IOException If the executable cannot be started in the configured directory.
     */
    private fun startProcess(command: List<String>): Process =
        synchronized(processLock) {
            check(!shuttingDown) { "SteamCMD service is shutting down" }
            check(activeProcess == null) { "A SteamCMD session is already running" }
            ProcessBuilder(command)
                .directory(File(properties.cmdRootPath))
                .redirectErrorStream(true)
                .start()
                .also { activeProcess = it }
        }

    /**
     * Reads SteamCMD output, records classified messages, and dispatches them to app consumers.
     *
     * Closes process input because commands are supplied as arguments. Passwords are redacted
     * after classification and before tracking or dispatch. Authentication failures terminate
     * the process and stop reading; the output reader is closed when this method exits.
     *
     * @param process Running SteamCMD process with standard error merged into standard output.
     * @param batch Tracker initialized with the same app order as the process command.
     * @throws java.io.IOException If a process stream cannot be read or closed.
     */
    private fun consumeOutput(
        process: Process,
        batch: SteamCmdBatchTracker,
    ) {
        process.outputStream.close()
        process.inputStream.bufferedReader().use { reader ->
            for (line in reader.lineSequence()) {
                val message = defaultOutputHandler.handle(listOf(line)) ?: continue
                val output = redact(message)
                val app = batch.record(output)
                if (app != null) consumers.forEach { it.accept(output, app) }

                if (output.type.isAuthenticationFailure) {
                    terminate(process)
                    break
                }
            }
        }
    }

    /**
     * Replaces literal occurrences of the configured password in the message text.
     * Classification and app ID are preserved so redaction cannot change result routing.
     *
     * @param output Classified message whose text may contain the configured password.
     * @return A copy with password occurrences replaced by `<REDACTED>`, or the original
     * message when the configured password is blank.
     */
    private fun redact(output: ResultMessage): ResultMessage =
        if (properties.password.isBlank()) {
            output
        } else {
            output.copy(message = output.message.replace(properties.password, "<REDACTED>"))
        }

    /**
     * Marks the service as shutting down and terminates any active SteamCMD session.
     *
     * Invoked when Spring destroys the bean. The shutdown flag is set under the process
     * lock before termination, ensuring subsequent start attempts are rejected. Repeated
     * calls leave the service closed to new sessions.
     */
    @PreDestroy
    private fun shutdown() {
        val process =
            synchronized(processLock) {
                shuttingDown = true
                activeProcess
            }
        process?.let { terminate(it) }
    }

    /**
     * Requests termination of the process and its current descendants.
     *
     * Waits up to five seconds for the parent to exit, then requests forced termination
     * if necessary. Any surviving descendants are also forcibly terminated. Interruptions
     * restore the thread's interrupt flag and force the parent to terminate.
     *
     * @param process Process to stop; an already terminated process is ignored.
     */
    private fun terminate(process: Process) {
        if (!process.isAlive) return
        val descendants = process.descendants().use { it.toList() }
        descendants.forEach { it.destroy() }
        process.destroy()
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly()
        } catch (_: InterruptedException) {
            process.destroyForcibly()
            Thread.currentThread().interrupt()
        } finally {
            descendants.filter { it.isAlive }.forEach { it.destroyForcibly() }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamCmdService::class.java)
    }
}
