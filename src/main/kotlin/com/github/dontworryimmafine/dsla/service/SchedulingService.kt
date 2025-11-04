package com.github.dontworryimmafine.dsla.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulingService(
    private val appIdResolutionService: AppIdResolutionService,
    private val steamCmdService: SteamCmdService,
) {
    @Scheduled(cron = "\${steam.schedule}")
    fun scheduledUpdate() {
        logger.info("Starting scheduled update")
        performUpdate()
    }

    fun performUpdate() {
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
