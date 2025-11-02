package com.github.dontworryimmafine.dsla.config

import com.github.dontworryimmafine.dsla.config.properties.HttpClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class HttpClientConfiguration {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    @ConfigurationProperties(prefix = "app.http-client.steam")
    fun steamHttpClientProperties(): HttpClientProperties {
        return HttpClientProperties()
    }
}
