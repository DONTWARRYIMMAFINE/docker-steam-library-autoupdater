package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    name = ["steam.cmd-filter-output"],
    havingValue = "false",
)
class DebugSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    /**
     * Prints the received message text to standard output without additional formatting.
     * The message has already been classified and redacted by the session reader.
     *
     * @param output Message whose text should be printed, regardless of classification.
     * @param steamApp Associated app supplied by the consumer contract; unused by this renderer.
     */
    override fun accept(
        output: ResultMessage,
        steamApp: SteamApp,
    ) {
        println(output.message)
    }
}
