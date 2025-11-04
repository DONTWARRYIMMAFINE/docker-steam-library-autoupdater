package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class ErrorOutputHandler : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? =
        output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.ERROR)
        }

    private fun match(line: String): Boolean =
        line.contains("error!", ignoreCase = true) ||
            line.contains("fail!", ignoreCase = true) ||
            line.contains("failed!", ignoreCase = true)
}
