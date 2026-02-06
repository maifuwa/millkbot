package com.bigboss.millkbot.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class AgentConfiguration {

    @Bean
    fun systemPromptTemplate(@Value($$"${prompt.path}") promptResource: Resource): SystemPromptTemplate {
        val templateText = promptResource.inputStream.bufferedReader().use { it.readText() }
        return SystemPromptTemplate(templateText)
    }

    @Bean
    fun chatClient(builder: ChatClient.Builder, systemPrompt: SystemPromptTemplate): ChatClient {
        return builder
            .defaultSystem(systemPrompt.template)
            .defaultAdvisors(SimpleLoggerAdvisor())
            .build()
    }
}