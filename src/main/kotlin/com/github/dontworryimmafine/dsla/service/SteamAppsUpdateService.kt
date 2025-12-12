package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.aop.ExecutionLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SteamAppsUpdateService(
    private val appIdResolutionService: AppIdResolutionService,
    private val steamCmdService: SteamCmdService,
    private val steamApiService: SteamApiService,
) {
    @ExecutionLock
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
        private val logger = LoggerFactory.getLogger(SteamAppsUpdateService::class.java)
    }
}
