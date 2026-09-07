package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class RecordingSteamCmdOutputConsumer : SteamCmdOutputConsumer {
    private val captured = CopyOnWriteArrayList<CapturedSteamCmdOutput>()
    private val downloadStarted = CountDownLatch(1)

    val messages: List<CapturedSteamCmdOutput>
        get() = captured.toList()

    override fun accept(
        output: ResultMessage,
        steamApp: SteamApp,
    ) {
        captured += CapturedSteamCmdOutput(steamApp.appId, output)
        if (output.type == MessageType.DOWNLOADING) downloadStarted.countDown()
    }

    fun awaitDownloadStarted() {
        check(downloadStarted.await(5, TimeUnit.SECONDS)) { "SteamCMD fixture did not start downloading within 5 seconds" }
    }
}

internal data class CapturedSteamCmdOutput(
    val appId: Long,
    val result: ResultMessage,
)
