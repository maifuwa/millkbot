package com.bigboss.millkbot.config

import org.ntqqrev.milky.MilkyClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfiguration(
    private val config: Config,
) {
    @Bean
    fun milkyClient(): MilkyClient {
        return MilkyClient(
            addressBase = config.botConfig.domain,
            accessToken = config.botConfig.token,
        )
    }
}