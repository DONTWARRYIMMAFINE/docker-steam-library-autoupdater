package com.github.dontworryimmafine.dsla.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulingService(
    private val appIdResolutionService: AppIdResolutionService,
    private val steamCmdService: SteamCmdService,
    private val steamApiService: SteamApiService,
) {
    @Scheduled(cron = "\${steam.schedule}")
    fun scheduledUpdate() {
        logger.info("Starting scheduled update")
        performUpdate()
    }

    fun performUpdate() {
        if (!steamApiService.isAppsUpdateAllowed()) {
            logger.warn("Update is not allowed right now.")
            return
        }

        try {
            val appIds = appIdResolutionService.resolveAppIds()
            steamCmdService.updateApps(appIds)
            logger.info("Update completed")
        } catch (e: Exception) {
            logger.error("Update failed", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchedulingService::class.java)
    }
}
