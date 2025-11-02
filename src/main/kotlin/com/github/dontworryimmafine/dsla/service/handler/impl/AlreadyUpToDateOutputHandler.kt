package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class AlreadyUpToDateOutputHandler : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        return output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.ALREADY_UP_TO_DATE)
        }
    }

    private fun match(line: String): Boolean =
        line.contains("already up to date", ignoreCase = true)
}
