package com.github.dontworryimmafine.dsla.config.properties

import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "steam")
data class SteamProperties
    @ConstructorBinding constructor(
        val schedule: String,
        val username: String,
        val password: String,
        val rootPath: String,
        val cmdRootPath: String,
        val cmdFilterOutput: String,
        val cmdValidateInstalled: Boolean,
        val appIdResolveStrategies: Set<AppIdResolveStrategyType>,
        val manualAppIds: Set<Long>,
        val ignoreAppIds: Set<Long>,
    )
