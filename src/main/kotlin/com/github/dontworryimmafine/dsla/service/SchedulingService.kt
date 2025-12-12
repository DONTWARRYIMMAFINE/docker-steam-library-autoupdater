package com.github.dontworryimmafine.dsla.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulingService(
    private val steamAppsUpdateService: SteamAppsUpdateService,
) {
    @Scheduled(cron = "\${steam.schedule}")
    fun scheduledUpdate() {
        logger.info("Starting scheduled update")
        steamAppsUpdateService.performUpdate()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchedulingService::class.java)
    }
}
