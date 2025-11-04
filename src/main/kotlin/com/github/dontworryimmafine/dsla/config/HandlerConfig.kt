package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.AlreadyUpToDateOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.CompositeOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.DownloadOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.ErrorOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.IncorrectPasswordOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.LastLineOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.NoCredentialCacheOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.SteamGuardTimeoutOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.SuccessOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.ValidationOutputHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HandlerConfig {
    @Bean
    fun defaultOutputHandler(): OutputHandler =
        CompositeOutputHandler(
            NoCredentialCacheOutputHandler(),
            IncorrectPasswordOutputHandler(),
            SteamGuardTimeoutOutputHandler(),
            ErrorOutputHandler(),
            AlreadyUpToDateOutputHandler(),
            SuccessOutputHandler(),
            LastLineOutputHandler(),
        )

    @Bean
    fun progressibleOutputHandler(): OutputHandler =
        CompositeOutputHandler(
            DownloadOutputHandler(),
            ValidationOutputHandler(),
        )
}
