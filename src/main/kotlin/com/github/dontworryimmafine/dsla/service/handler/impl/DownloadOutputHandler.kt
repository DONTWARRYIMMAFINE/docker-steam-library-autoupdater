package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class DownloadOutputHandler(override val next: OutputHandler? = null) : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        output.lastOrNull { match(it) }?.let {
            return ResultMessage(it, MessageType.DOWNLOADING)
        }
        return proceed(output)
    }

    private fun match(line: String): Boolean =
        line.contains("downloading, progress", ignoreCase = true)
}
