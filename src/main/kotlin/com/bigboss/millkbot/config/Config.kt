package com.bigboss.millkbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class Config(
    val botConfig: BotConfig,
    val coroutineConfig: CoroutineConfig
)

data class BotConfig(
    val domain: String,
    val token: String,
)


data class CoroutineConfig(
    val queueCapacity: Int,
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val keepAliveSeconds: Int,
)