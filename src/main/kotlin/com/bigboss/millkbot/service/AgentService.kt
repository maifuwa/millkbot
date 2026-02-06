package com.bigboss.millkbot.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ntqqrev.milky.OutgoingSegment
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Service

@Service
class AgentService(
    private val chatClient: ChatClient,
    private val userService: UserService,
) {

    suspend fun chat(id: Long, name: String, message: String): List<OutgoingSegment> {
        val user = withContext(Dispatchers.IO) {
            userService.getUser(id, name)
        }

        val userMessage = UserMessage(
            """
            userInfo : 
                id: ${user.id}
                name: ${user.name}
                relation: ${user.relation}
                ${if (user.customPrompt == null) "" else "custom prompt: " + user.customPrompt}
            message: ${message.trimIndent()}
        """.trimIndent()
        )

        val response = chatClient.prompt()
            .user(userMessage.text)
            .call()
            .content() ?: return emptyList()

        return listOf(OutgoingSegment.Text(OutgoingSegment.Text.Data(response)))
    }
}
