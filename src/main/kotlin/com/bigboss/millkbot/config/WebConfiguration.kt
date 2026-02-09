package com.bigboss.millkbot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class WebConfiguration {

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder().build()
    }
}
