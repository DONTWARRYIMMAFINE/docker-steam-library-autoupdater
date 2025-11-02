package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class DownloadOutputHandler : OutputHandler {
    override fun handle(output: List<String>): ResultMessage? {
        return output.lastOrNull { match(it) }?.let {
            ResultMessage(it, MessageType.DOWNLOADING)
        }
    }

    private fun match(line: String): Boolean =
        line.contains("downloading, progress", ignoreCase = true)
}
