package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppUpdateResult
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@Service
class SteamCmdService(
    private val properties: SteamProperties,
    private val steamCmdCommandService: SteamCmdCommandService,
    private val consumers: List<SteamCmdOutputConsumer>,
    private val defaultOutputHandler: OutputHandler
) {
    private val steamCmdProcess = AtomicReference<Process?>(null)

    fun updateApps(appIds: Set<Long>): Map<Long, AppUpdateResult> {
        if (appIds.isEmpty()) {
            logger.warn("No app IDs to update")
            return emptyMap()
        }

        logger.info("Starting updates for ${appIds.size} apps")

        val stopConditions = setOf(MessageType.INCORRECT_PASSWORD, MessageType.STEAM_GUARD_TIMEOUT)
        val results = appIds.asSequence()
            .map { appId -> appId to updateApp(appId).also { logger.info("Starting update for appId: $appId") } }
            .takeWhile { (_, result) -> result.resultMessage.type !in stopConditions }
            .toMap()

        logResults(results, appIds.size)
        return results
    }

    private fun updateApp(appId: Long, useFreshSession: Boolean = false): AppUpdateResult {
        if (useFreshSession) {
            logger.warn("No steam cache found for ${properties.username}. Don't forget to accept SteamGuard request.")
        }

        val startTime = System.currentTimeMillis()
        return try {
            val command = steamCmdCommandService
                .buildAppUpdateCommand(appId, useFreshSession, properties.cmdValidateInstalled)
            val resultMessage = runSteamCmd(command, appId)

            val duration = System.currentTimeMillis() - startTime
            when (resultMessage.type) {
                MessageType.NO_CREDENTIAL_CACHE -> {
                    updateApp(appId, true)
                }

                MessageType.ALREADY_UP_TO_DATE, MessageType.SUCCESS -> {
                    logger.info(resultMessage.message)
                    AppUpdateResult(appId, resultMessage, duration = duration)
                }

                else -> {
                    logger.error(resultMessage.message)
                    AppUpdateResult(appId, resultMessage, duration = duration)
                }
            }
        } catch (ex: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Error updating appId: $appId", ex)
            AppUpdateResult(
                appId,
                resultMessage = ResultMessage(ex.message ?: "Unknown error", MessageType.ERROR),
                duration = duration
            )
        }
    }

    private fun runSteamCmd(command: List<String>, appId: Long = -1L): ResultMessage {
        val process = ProcessBuilder(command).apply {
            directory(File(properties.cmdRootPath))
            redirectErrorStream(true)
        }.start()
        steamCmdProcess.set(process)

        val future = CompletableFuture.supplyAsync {
            val output = StringBuilder()
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    consumers.forEach { it.accept(line, appId) }
                    output.append(line.trim()).append('\n')
                }
            }
            output.toString()
        }

        process.waitFor()
        return defaultOutputHandler.handle(future.get().lines()) ?: ResultMessage()
    }

    private fun logResults(results: Map<Long, AppUpdateResult>, totalApps: Int) {
        val success = results.values.filter { it.resultMessage.type == MessageType.SUCCESS }
        val failed = results.values.filter {
            it.resultMessage.type != MessageType.SUCCESS &&
            it.resultMessage.type != MessageType.ALREADY_UP_TO_DATE
        }

        if (failed.isNotEmpty()) {
            failed.forEach {
                logger.warn("AppId ${it.appId} failed: ${it.resultMessage.message} (${it.duration}ms)")
            }
        }

        logger.info("Update summary: ${success.size} successful, ${totalApps - success.size - failed.size} skipped, ${failed.size} failed out of $totalApps total")
    }

    @PreDestroy
    private fun shutdown() {
        steamCmdProcess.get()?.destroy()
        steamCmdProcess.set(null)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamCmdService::class.java)
    }
}
