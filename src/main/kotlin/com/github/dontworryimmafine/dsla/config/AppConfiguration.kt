package com.github.dontworryimmafine.dsla.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {
    @Bean
    fun objectMapper(): ObjectMapper =
        jacksonObjectMapper()
            .registerKotlinModule()
}
