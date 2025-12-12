package com.github.dontworryimmafine.dsla.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.support.locks.DefaultLockRegistry

@Configuration
class LockConfiguration {
    @Bean
    fun lockRegistry(): DefaultLockRegistry = DefaultLockRegistry()
}
