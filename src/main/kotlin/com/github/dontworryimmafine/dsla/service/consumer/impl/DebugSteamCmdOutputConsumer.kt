package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    name = ["steam.cmd-filter-output"],
    havingValue = "false"
)
class DebugSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    override fun accept(line: String, appid: Long) {
        println(line)
    }
}
