package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class NoCredentialCacheOutputHandler: OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        return output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.NO_CREDENTIAL_CACHE)
        }
    }

    private fun match(line: String): Boolean =
        line.contains("no cached credentials", ignoreCase = true)
}
