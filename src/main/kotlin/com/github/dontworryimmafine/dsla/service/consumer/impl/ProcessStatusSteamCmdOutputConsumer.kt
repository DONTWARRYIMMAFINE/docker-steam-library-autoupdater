package com.github.dontworryimmafine.dsla.service.consumer.impl

import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.extension.toDownloadUnit
import com.github.dontworryimmafine.dsla.utils.SteamCmdUtils
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class ProcessStatusSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    override fun accept(line: String, appid: Long) {
        if (SteamCmdUtils.isProcessMessage(line)) {
            val (current, total) = SteamCmdUtils.parseProcessMessage(line)
            val totalDownloadUnit = total.toDownloadUnit()
            val currentDownloadUnit = current.toDownloadUnit(totalDownloadUnit.type)

            val processType = SteamCmdUtils.determineProcessType(line)
            val percent = (current * 100 / max(total, 1.0)).coerceAtMost(100.0)

            val progressWidth = 30
            val filledWidth = (percent * progressWidth / 100).toInt()
            val progress = "=".repeat(filledWidth) + " ".repeat(progressWidth - filledWidth)
            println("[$appid] $processType [$progress] ${"%.2f".format(percent)}% | $currentDownloadUnit / $totalDownloadUnit")
        }
    }
}
