package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import org.springframework.stereotype.Component

@Component
class DebugSteamCmdOutputConsumer(
    private val properties: SteamProperties
) : SteamCmdOutputConsumer {
    override fun accept(line: String, appid: Long) {
        if (!properties.cmdFilterOutput) {
            println(line)
        }
    }
}
