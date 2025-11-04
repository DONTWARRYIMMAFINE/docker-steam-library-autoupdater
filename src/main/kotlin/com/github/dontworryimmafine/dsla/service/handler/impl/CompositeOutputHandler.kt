package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class CompositeOutputHandler(
    private vararg val handlers: OutputHandler,
) : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? = handlers.firstNotNullOfOrNull { it.handle(output) }
}
