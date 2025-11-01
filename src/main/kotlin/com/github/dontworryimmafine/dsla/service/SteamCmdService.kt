package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppUpdateResult
import com.github.dontworryimmafine.dsla.model.AppUpdateStatus
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.SteamCmdProcessResult
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.utils.SteamCmdUtils
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
    private val consumers: List<SteamCmdOutputConsumer>
) {
    private val steamCmdProcess = AtomicReference<Process?>(null)

    fun updateApps(appIds: Set<Long>): Map<Long, AppUpdateResult> {
        if (appIds.isEmpty()) {
            logger.warn("No app IDs to update")
            return emptyMap()
        }

        logger.info("Starting updates for ${appIds.size} apps")

        val results = appIds.map { appId ->
            logger.info("Starting update for appId: $appId")
            updateApp(appId)
        }.associateBy { it.appId }

        val success = results.values.filter { it.status == AppUpdateStatus.SUCCESS }
        val failed = results.values.filter { it.status == AppUpdateStatus.FAILED }
        if (failed.isNotEmpty()) {
            failed.forEach {
                logger.warn("AppId ${it.appId} failed: ${it.errorMessage} (${it.duration}ms)")
            }
        }

        logger.info("Update summary: ${success.size} successful, ${failed.size} failed out of ${results.size} total")
        return results
    }

    private fun updateApp(appId: Long, useFreshSession: Boolean = false): AppUpdateResult {
        val startTime = System.currentTimeMillis()

        return try {
            if (useFreshSession) {
                logger.warn("No steam cache found for ${properties.username}. Don't forget to accept SteamGuard request.")
            }

            val command = steamCmdCommandService.buildAppUpdateCommand(appId, useFreshSession, properties.cmdValidateInstalled)
            val (exitCode, resultMessage) = runSteamCmd(command, appId)
            val duration = System.currentTimeMillis() - startTime
            when {
                SteamCmdUtils.isNoCredentialCache(resultMessage.message) -> {
                    updateApp(appId, true)
                }

                exitCode == 0 && resultMessage.type == MessageType.SUCCESS -> {
                    logger.info(resultMessage.message)
                    AppUpdateResult(appId, AppUpdateStatus.SUCCESS, duration = duration)
                }

                else -> {
                    logger.error(resultMessage.message)
                    AppUpdateResult(appId, AppUpdateStatus.FAILED, duration = duration)
                }
            }
        } catch (ex: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Error updating appId: $appId", ex)
            AppUpdateResult(
                appId,
                AppUpdateStatus.FAILED,
                errorMessage = ex.message ?: "Unknown error",
                duration = duration
            )
        }
    }

    private fun runSteamCmd(command: List<String>, appId: Long = -1L): SteamCmdProcessResult {
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

        return SteamCmdProcessResult(
            process.waitFor(),
            SteamCmdUtils.extractResultMessage(future.get().lines())
        )
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
