package com.bigboss.millkbot.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class CoroutineConfiguration(
    private val config: Config
) {

    @Bean
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor().apply {
            queueCapacity = config.coroutineConfig.queueCapacity
            corePoolSize = config.coroutineConfig.corePoolSize
            maxPoolSize = config.coroutineConfig.maxPoolSize
            keepAliveSeconds = config.coroutineConfig.keepAliveSeconds
        }
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }

    @Bean
    fun springDispatcher(executor: Executor): CoroutineDispatcher {
        return (executor as ThreadPoolTaskExecutor).asCoroutineDispatcher()
    }

    @Bean
    fun applicationScope(springDispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(springDispatcher + SupervisorJob())
    }
}