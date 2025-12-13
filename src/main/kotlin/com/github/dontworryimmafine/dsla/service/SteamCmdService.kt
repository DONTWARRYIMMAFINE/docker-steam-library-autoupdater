package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.consumer.SteamCmdOutputConsumer
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@Service
class SteamCmdService(
    private val properties: SteamProperties,
    private val consumers: List<SteamCmdOutputConsumer>,
    private val defaultOutputHandler: OutputHandler,
) {
    private val steamCmdProcess = AtomicReference<Process?>(null)

    fun runSteamCmd(
        command: List<String>,
        steamApp: SteamApp,
    ): ResultMessage {
        logger.info("[$steamApp] Starting steamCmd process")

        val process =
            ProcessBuilder(command)
                .apply {
                    directory(File(properties.cmdRootPath))
                    redirectErrorStream(true)
                }.start()
        steamCmdProcess.set(process)

        val future =
            CompletableFuture.supplyAsync {
                val output = StringBuilder()
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        consumers.forEach { it.accept(line, steamApp) }
                        output.append(line.trim()).append('\n')
                    }
                }
                output.toString()
            }

        process.waitFor()
        return defaultOutputHandler.handle(future.get().lines()) ?: ResultMessage()
    }

    @PreDestroy
    private fun shutdown() {
        steamCmdProcess.get()?.destroy()
        steamCmdProcess.set(null)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamCmdService::class.java)
    }
}
