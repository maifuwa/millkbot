package com.bigboss.millkbot.service

import com.bigboss.millkbot.converter.ReplyListOutputConverter
import com.bigboss.millkbot.tool.GetCurrentTimeTool
import com.bigboss.millkbot.util.MessageTextConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service

@Service
class AgentService(
    private val chatClient: ChatClient,
    private val userService: UserService,
) {

    suspend fun chat(id: Long, name: String, message: String): List<String> {
        val user = withContext(Dispatchers.IO) {
            userService.getUser(id, name)
        }

        val userMessage = message.trim()
        val agentContextMessage = MessageTextConverter.buildAgentContextMessage(user)

        val outputConverter = ReplyListOutputConverter()
        val getCurrentTimeTool = GetCurrentTimeTool()
        val conversationId = "friend-$id"

        val replies = withContext(Dispatchers.IO) {
            chatClient.prompt()
                .advisors { advisor ->
                    advisor.param(ChatMemory.CONVERSATION_ID, conversationId)
                }
                .messages(SystemMessage(agentContextMessage))
                .user(userMessage)
                .tools(getCurrentTimeTool)
                .call()
                .entity(outputConverter)
                ?: emptyList()
        }

        return replies
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
