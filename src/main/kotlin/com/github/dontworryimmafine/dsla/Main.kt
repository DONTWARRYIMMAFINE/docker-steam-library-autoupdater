package com.github.dontworryimmafine.dsla

import com.github.dontworryimmafine.dsla.service.SteamAppsUpdateService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Main(
    private val steamAppsUpdateService: SteamAppsUpdateService,
) : CommandLineRunner {
    @PostConstruct
    fun init() {
        logger.info("Steam Library Autoupdater initialized")
    }

    override fun run(vararg args: String?) {
        logger.info("Steam Library Autoupdater started - performing initial update")
        steamAppsUpdateService.performUpdate()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
