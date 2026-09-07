package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(SteamProperties::class)
class SteamConfig {
    /**
     * Provides the clock used to calculate and check the SteamCMD login cooldown.
     *
     * @return A system clock in UTC, supplied as a bean so tests can replace the time source.
     */
    @Bean
    fun steamCmdClock(): Clock = Clock.systemUTC()

    @Bean
    fun appIdResolveStrategyMap(appIdResolveStrategies: List<AppIdResolveStrategy>): Map<AppIdResolveStrategyType, AppIdResolveStrategy> =
        appIdResolveStrategies.associateBy { it.getType() }
}
