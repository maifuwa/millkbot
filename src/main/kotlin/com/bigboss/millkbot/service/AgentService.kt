package com.bigboss.millkbot.service

import com.bigboss.millkbot.tool.GetCurrentTimeTool
import com.bigboss.millkbot.tool.SendMessageTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ntqqrev.milky.MilkyClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class AgentService(
    private val chatClient: ChatClient,
    private val userService: UserService,
    private val milkyClient: MilkyClient,
) {

    suspend fun chat(id: Long, name: String, message: String) {
        val user = withContext(Dispatchers.IO) {
            userService.getUser(id, name)
        }

        val userMessage = """
            userInfo :
                id: ${user.id}
                name: ${user.name}
                relation: ${user.relation}
                ${if (user.customPrompt == null) "" else "custom prompt: " + user.customPrompt}
            message: ${message.trimIndent()}
        """.trimIndent()

        val sendMessageTool = SendMessageTool(milkyClient, id)
        val getCurrentTimeTool = GetCurrentTimeTool()

        withContext(Dispatchers.IO) {
            chatClient.prompt()
                .user(userMessage)
                .tools(sendMessageTool, getCurrentTimeTool)
                .call()
                .content()
        }
    }
}
