package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AppUpdateSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    /**
     * Logs successful app completions at INFO level and app errors at ERROR level.
     * Messages without an app ID and messages that do not describe a completion are ignored.
     *
     * @param output Classified, redacted SteamCMD message to inspect.
     * @param steamApp App associated with the message, used as the log entry's label.
     */
    override fun accept(
        output: ResultMessage,
        steamApp: SteamApp,
    ) {
        if (output.appId == null) return

        if (output.type.isSuccessful) {
            logger.info("[$steamApp] ${output.message}")
        } else if (output.type == MessageType.ERROR) {
            logger.error("[$steamApp] ${output.message}")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AppUpdateSteamCmdOutputConsumer::class.java)
    }
}
