package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.service.handler.OutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.AlreadyUpToDateOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.AppUpdateOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.AppUpdateStartedOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.CompositeOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.DownloadOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.ErrorOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.IncorrectPasswordOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.LastLineOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.LoginFailureOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.NoCredentialCacheOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.SteamGuardTimeoutOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.SuccessOutputHandler
import com.github.dontworryimmafine.dsla.service.handler.impl.ValidationOutputHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HandlerConfig {
    /**
     * Builds the handler chain used to classify SteamCMD output.
     *
     * App commands and results take precedence over authentication errors, so an app error
     * cannot be mistaken for a failed login. Unrecognized output falls back to its last line.
     *
     * @return A composite handler that returns the first matching classification.
     */
    @Bean
    fun defaultOutputHandler(): OutputHandler =
        CompositeOutputHandler(
            AppUpdateStartedOutputHandler(),
            appUpdateOutputHandler(),
            NoCredentialCacheOutputHandler(),
            IncorrectPasswordOutputHandler(),
            SteamGuardTimeoutOutputHandler(),
            LoginFailureOutputHandler(),
            progressibleOutputHandler(),
            LastLineOutputHandler(),
        )

    /**
     * Builds a handler for app errors, apps already up to date, and successful updates.
     * Only messages containing a recognizable app ID can produce an app result.
     *
     * @return A handler that attaches the parsed app ID to the recognized result.
     */
    @Bean
    fun appUpdateOutputHandler(): OutputHandler =
        AppUpdateOutputHandler(
            CompositeOutputHandler(
                ErrorOutputHandler(),
                AlreadyUpToDateOutputHandler(),
                SuccessOutputHandler(),
            ),
        )

    @Bean
    fun progressibleOutputHandler(): OutputHandler =
        CompositeOutputHandler(
            DownloadOutputHandler(),
            ValidationOutputHandler(),
        )
}
