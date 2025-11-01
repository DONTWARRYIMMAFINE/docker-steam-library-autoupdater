package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.service.strategy.AppIdResolveStrategy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SteamProperties::class)
class SteamConfig {
    @Bean
    fun appIdResolveStrategyMap(appIdResolveStrategies: List<AppIdResolveStrategy>): Map<AppIdResolveStrategyType, AppIdResolveStrategy> {
        return appIdResolveStrategies.associateBy { it.getType() }
    }
}
