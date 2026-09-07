package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.extension.toDownloadUnit
import com.github.dontworryimmafine.dsla.extension.toProgressible
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class ProgressibleSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    /**
     * Prints an app's download or validation progress as a percentage, bar, and byte totals.
     *
     * Other message types are ignored. Unparseable progress uses zero totals, and the
     * percentage calculation guards against division by zero and values above 100 percent.
     *
     * @param output Classified, redacted message containing SteamCMD progress counters.
     * @param steamApp App whose name or ID labels the progress line.
     */
    override fun accept(
        output: ResultMessage,
        steamApp: SteamApp,
    ) {
        if (output.type != MessageType.DOWNLOADING && output.type != MessageType.VALIDATING) return

        val (current, total) = output.message.toProgressible()
        val totalDownloadUnit = total.toDownloadUnit()
        val currentDownloadUnit = current.toDownloadUnit(totalDownloadUnit.type)
        val percent = (current * 100 / max(total, 1.0)).coerceAtMost(100.0)

        val filledWidth = (percent * PROGRESS_BAR_WIDTH / 100).toInt()
        val progress = "=".repeat(filledWidth) + " ".repeat(PROGRESS_BAR_WIDTH - filledWidth)
        println("[$steamApp] ${output.type} [$progress] ${"%.2f".format(percent)}% | $currentDownloadUnit / $totalDownloadUnit")
    }

    companion object {
        private const val PROGRESS_BAR_WIDTH = 30
    }
}
