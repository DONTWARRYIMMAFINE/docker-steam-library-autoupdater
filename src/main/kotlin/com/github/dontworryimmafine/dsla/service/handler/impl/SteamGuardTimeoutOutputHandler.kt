package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class SteamGuardTimeoutOutputHandler : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? =
        output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.STEAM_GUARD_TIMEOUT)
        }

    private fun match(line: String): Boolean = line.contains("wait for confirmation timed out", ignoreCase = true)
}
