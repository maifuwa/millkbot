package com.bigboss.millkbot.service

import org.ntqqrev.milky.OutgoingSegment
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service

@Service
class AgentService(
    private val chatClient: ChatClient,
    private val userService: UserService,
    private val systemPrompt: SystemPromptTemplate,
) {

    suspend fun chat(id: Long, name: String, message: String): List<OutgoingSegment> {
        val user = userService.getUser(id, name)

        val systemMessage = systemPrompt.createMessage(
            mapOf(
                "user_role" to user.relation,
                "user_name" to user.name,
                "current_context" to message,
            )
        )

        return chatClient.prompt()
            .user(systemMessage.text)
            .call()
            .entity(object : ParameterizedTypeReference<List<OutgoingSegment>>() {}) ?: emptyList()
    }
}
