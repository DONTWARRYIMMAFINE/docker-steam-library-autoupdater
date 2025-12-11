package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.extension.toDownloadUnit
import com.github.dontworryimmafine.dsla.extension.toProgressible
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class ProgressibleSteamCmdOutputConsumer(
    private val progressibleOutputHandler: OutputHandler,
) : SteamCmdOutputConsumer {
    override fun accept(
        line: String,
        steamApp: SteamApp,
    ) {
        val resultMessage = progressibleOutputHandler.handle(listOf(line))
        if (resultMessage?.type == MessageType.DOWNLOADING || resultMessage?.type == MessageType.VALIDATING) {
            val (current, total) = line.toProgressible()
            val totalDownloadUnit = total.toDownloadUnit()
            val currentDownloadUnit = current.toDownloadUnit(totalDownloadUnit.type)

            val percent = (current * 100 / max(total, 1.0)).coerceAtMost(100.0)

            val filledWidth = (percent * PROGRESS_BAR_WIDTH / 100).toInt()
            val progress = "=".repeat(filledWidth) + " ".repeat(PROGRESS_BAR_WIDTH - filledWidth)
            print("[$steamApp] ${resultMessage.type} [$progress] ${"%.2f".format(percent)}% | $currentDownloadUnit / $totalDownloadUnit\r")
        }
    }

    companion object {
        private const val PROGRESS_BAR_WIDTH = 30
    }
}
