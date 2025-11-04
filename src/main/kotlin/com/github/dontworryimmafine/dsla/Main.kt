package com.github.dontworryimmafine.dsla

import com.github.dontworryimmafine.dsla.service.SchedulingService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class Main(
    private val schedulingService: SchedulingService,
) : CommandLineRunner {
    @PostConstruct
    fun init() {
        logger.info("Steam Library Autoupdater initialized")
    }

    override fun run(vararg args: String?) {
        logger.info("Steam Library Autoupdater started - performing initial update")
        schedulingService.performUpdate()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
