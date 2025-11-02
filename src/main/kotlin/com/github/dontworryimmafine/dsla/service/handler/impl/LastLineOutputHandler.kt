package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class LastLineOutputHandler(override val next: OutputHandler? = null) : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        val message = output.lastOrNull() ?: return null
        return ResultMessage(message, MessageType.ERROR)
    }
}
