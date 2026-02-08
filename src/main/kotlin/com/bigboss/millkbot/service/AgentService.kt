package com.bigboss.millkbot.service

import com.bigboss.millkbot.converter.ReplyListOutputConverter
import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.util.MessageTextConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.stereotype.Service

@Service
class AgentService(
    private val chatClient: ChatClient,
    private val replyListOutputConverter: ReplyListOutputConverter,
) {

    suspend fun chat(user: User, message: String): List<String> {
        val agentContextMessage = MessageTextConverter.buildAgentContextMessage(user)
        val conversationId = "${user.relation}-${user.id}"

        val replies = withContext(Dispatchers.IO) {
            chatClient.prompt()
                .advisors { advisor ->
                    advisor.param(ChatMemory.CONVERSATION_ID, conversationId)
                }
                .messages(SystemMessage(agentContextMessage))
                .user(message.trim())
                .call()
                .entity(replyListOutputConverter)
                ?: emptyList()
        }

        return replies
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    suspend fun deal(user: User, taskContent: String): List<String> {
        val agentContextMessage = MessageTextConverter.buildAgentContextMessage(user)

        val replies = withContext(Dispatchers.IO) {
            chatClient.prompt()
                .messages(SystemMessage(agentContextMessage))
                .user(taskContent.trim())
                .call()
                .entity(replyListOutputConverter)
                ?: emptyList()
        }

        return replies
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
