package com.bigboss.millkbot.config

import com.bigboss.millkbot.tool.ScheduleTools
import com.bigboss.millkbot.tool.SearchTools
import com.bigboss.millkbot.tool.TimeTools
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class AgentConfiguration {

    @Bean
    fun systemPromptTemplate(@Value("\${prompt.path}") promptResource: Resource): SystemPromptTemplate {
        val templateText = promptResource.inputStream.bufferedReader().use { it.readText() }
        return SystemPromptTemplate(templateText)
    }

    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        systemPrompt: SystemPromptTemplate,
        chatMemory: ChatMemory,
        timeTools: TimeTools,
        searchTools: SearchTools,
        scheduleTools: ScheduleTools
    ): ChatClient {
        val chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build()

        return builder
            .defaultSystem(systemPrompt.template)
            .defaultAdvisors(SimpleLoggerAdvisor(), chatMemoryAdvisor)
            .defaultTools(timeTools, searchTools, scheduleTools)
            .build()
    }

    @Bean
    fun chatMemory(chatMemoryRepository: org.springframework.ai.chat.memory.ChatMemoryRepository): ChatMemory {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(20)
            .build()
    }
}
