package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HandlerConfig {
    @Bean
    fun defaultOutputHandler(): OutputHandler {
        return NoCredentialCacheOutputHandler(
            IncorrectPasswordOutputHandler(
                SteamGuardTimeoutOutputHandler(
                    ErrorOutputHandler(
                        SuccessOutputHandler(
                            LastLineOutputHandler()
                        )
                    )
                )
            )
        )
    }

    @Bean
    fun progressibleOutputHandler(): OutputHandler {
        return DownloadOutputHandler(ValidationOutputHandler())
    }
}
