package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class ValidationOutputHandler : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? =
        output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.VALIDATING)
        }

    private fun match(line: String): Boolean =
        line.contains("verifying update, progress", ignoreCase = true) ||
            line.contains("verifying install, progress", ignoreCase = true)
}
