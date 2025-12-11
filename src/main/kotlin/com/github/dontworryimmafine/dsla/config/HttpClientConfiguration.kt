package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.config.properties.HttpClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class HttpClientConfiguration {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    @ConfigurationProperties(prefix = "app.http-client.steam-store")
    fun steamStoreHttpClientProperties(): HttpClientProperties = HttpClientProperties()

    @Bean
    @ConfigurationProperties(prefix = "app.http-client.steam-api")
    fun steamApiHttpClientProperties(): HttpClientProperties = HttpClientProperties()
}
