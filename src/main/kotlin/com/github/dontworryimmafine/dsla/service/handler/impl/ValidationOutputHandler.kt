package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class ValidationOutputHandler(override val next: OutputHandler? = null) : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        output.lastOrNull { match(it) }?.let {
            return ResultMessage(it, MessageType.VALIDATING)
        }
        return proceed(output)
    }

    private fun match(line: String): Boolean =
        line.contains("verifying update, progress", ignoreCase = true) ||
        line.contains("verifying install, progress", ignoreCase = true)
}
