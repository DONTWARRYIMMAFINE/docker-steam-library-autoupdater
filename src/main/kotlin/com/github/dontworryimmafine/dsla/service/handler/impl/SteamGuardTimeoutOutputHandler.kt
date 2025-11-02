package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class SteamGuardTimeoutOutputHandler(override val next: OutputHandler? = null) : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        output.lastOrNull { match(it) }?.let {
            return ResultMessage(it, MessageType.STEAM_GUARD_TIMEOUT)
        }
        return proceed(output)
    }

    private fun match(line: String): Boolean =
        line.contains("wait for confirmation timed out", ignoreCase = true)
}
