package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.aop.ExecutionLock
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppUpdateResult
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.resolver.SteamAppResolver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SteamAppsUpdateService(
    private val properties: SteamProperties,
    private val steamApiService: SteamApiService,
    private val appIdResolutionService: AppIdResolutionService,
    private val steamAppResolver: SteamAppResolver,
    private val steamCmdService: SteamCmdService,
    private val steamCmdCommandService: SteamCmdCommandService,
) {
    @ExecutionLock
    fun performUpdate() {
        if (!steamApiService.hasAllowedToUpdateState()) {
            logger.warn("Current steam user state is not allowed. Update will not be performed.")
            return
        }

        try {
            val appIds = appIdResolutionService.resolveAppIds()
            updateApps(appIds)
            logger.info("Update completed")
        } catch (ex: Exception) {
            logger.error("Update failed. Reason: ${ex.message}")
        }
    }

    private fun updateApps(appIds: Set<Long>): Map<SteamApp, AppUpdateResult> {
        if (appIds.isEmpty()) {
            logger.warn("No applications to update")
            return emptyMap()
        }

        val steamApps = steamAppResolver.resolve(appIds)
        val stopConditions = setOf(MessageType.INCORRECT_PASSWORD, MessageType.STEAM_GUARD_TIMEOUT)
        val results =
            steamApps
                .asSequence()
                .map { steamApp ->
                    logger.info("[$steamApp] Starting update")
                    steamApp to updateApp(steamApp)
                }.takeWhile { (_, result) -> result.resultMessage.type !in stopConditions }
                .toMap()

        logResults(results, steamApps.size)
        return results
    }

    private fun updateApp(
        steamApp: SteamApp,
        useFreshSession: Boolean = false,
    ): AppUpdateResult {
        if (useFreshSession) {
            logger.warn("No steam cache found for ${properties.username}. Don't forget to accept SteamGuard request.")
        }

        val startTime = System.currentTimeMillis()
        return try {
            val command =
                steamCmdCommandService
                    .buildAppUpdateCommand(steamApp.appId, useFreshSession, properties.cmdValidateInstalled)
            val resultMessage = steamCmdService.runSteamCmd(command, steamApp)

            val duration = System.currentTimeMillis() - startTime
            when (resultMessage.type) {
                MessageType.NO_CREDENTIAL_CACHE -> {
                    updateApp(steamApp, true)
                }

                MessageType.ALREADY_UP_TO_DATE, MessageType.SUCCESS -> {
                    logger.info("[$steamApp] ${resultMessage.message}")
                    AppUpdateResult(steamApp, resultMessage, duration = duration)
                }

                else -> {
                    logger.error("[$steamApp] ${resultMessage.message}")
                    AppUpdateResult(steamApp, resultMessage, duration = duration)
                }
            }
        } catch (ex: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Error updating $steamApp", ex)
            AppUpdateResult(
                steamApp,
                resultMessage = ResultMessage(ex.message ?: "Unknown error", MessageType.ERROR),
                duration = duration,
            )
        }
    }

    private fun logResults(
        results: Map<SteamApp, AppUpdateResult>,
        totalApps: Int,
    ) {
        val success = results.values.filter { it.resultMessage.type == MessageType.SUCCESS }
        val failed =
            results.values.filter {
                it.resultMessage.type != MessageType.SUCCESS &&
                    it.resultMessage.type != MessageType.ALREADY_UP_TO_DATE
            }

        if (failed.isNotEmpty()) {
            failed.forEach {
                logger.warn("${it.steamApp} failed: ${it.resultMessage.message} (${it.duration}ms)")
            }
        }

        logger.info(
            "Update summary: ${success.size} successful, ${totalApps - success.size - failed.size} skipped, ${failed.size} failed out of $totalApps total",
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamAppsUpdateService::class.java)
    }
}
