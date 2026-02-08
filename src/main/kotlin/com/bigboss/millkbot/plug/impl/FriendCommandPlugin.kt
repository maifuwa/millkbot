package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.plug.ProcessedMessage
import com.bigboss.millkbot.service.UserService
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.ntqqrev.milky.text
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
class FriendCommandPlugin(
    private val milkyClient: MilkyClient,
    private val userService: UserService
) : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        logger.debug("handle message from {}", msg.original.senderId)

        val content = msg.convertedText?.trim() ?: return true
        val user = msg.user ?: return true

        if (!content.startsWith(COMMAND_PREFIX)) {
            return true
        }

        val senderId = msg.original.senderId
        val commandInput = parseCommandInput(content)
        val response = when (commandInput.name) {
            "create_master" -> handleCreateMaster(senderId)
            "create_prompt" -> handleCreatePrompt(senderId, commandInput.args)
            "update_user" -> handleUpdateUser(user, commandInput.args)
            "all", "help" -> helpText()
            else -> "未知命令，请使用 #all 查看所有可用命令"
        }

        milkyClient.sendPrivateMessage(senderId) {
            text(response)
        }

        return false
    }

    private fun parseCommandInput(content: String): CommandInput {
        val commandLine = content.removePrefix(COMMAND_PREFIX).trim()
        if (commandLine.isBlank()) return CommandInput("all", null)

        val parts = commandLine.split(Regex("\\s+"), limit = 2)
        val name = parts.firstOrNull().orEmpty()
        val args = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        return CommandInput(name, args)
    }

    private fun handleCreateMaster(senderId: Long): String {
        return if (userService.createMaster(senderId)) {
            "创建 Master 用户成功"
        } else {
            "创建 Master 用户失败"
        }
    }

    private fun handleCreatePrompt(senderId: Long, prompt: String?): String {
        val promptText = prompt?.trim().orEmpty()
        if (promptText.isBlank()) return USAGE_CREATE_PROMPT
        return if (userService.createPrompt(senderId, promptText)) {
            "设置自定义提示词成功"
        } else {
            "设置自定义提示词失败"
        }
    }

    private fun handleUpdateUser(operator: User, updateArgs: String?): String {
        val parsedArgs = parseUpdateUserArgs(updateArgs) ?: return USAGE_UPDATE_USER
        val targetUserId = parsedArgs.userId
        val relation = parsedArgs.relation

        return if (userService.updateUserRelation(operator, targetUserId, relation)) {
            "更新用户关系成功"
        } else {
            "更新用户关系失败"
        }
    }

    private fun parseUpdateUserArgs(updateArgs: String?): UpdateUserArgs? {
        val source = updateArgs?.trim().orEmpty()
        if (source.isBlank()) return null

        val parts = source.split(Regex("\\s+"), limit = 2)
        if (parts.size < 2) return null

        val userId = parts[0].toLongOrNull() ?: return null
        val relation = parts[1].trim()
        if (relation.isBlank()) return null

        return UpdateUserArgs(userId, relation)
    }

    private fun helpText(): String {
        return """
           |可用命令列表:
           |1. #create_master - 创建 master 用户
           |2. #create_prompt <prompt> - 设置自定义提示词
           |3. #update_user <user_id> <relation> - 修改用户关系
           |4. #all - 查看所有命令
        """.trimMargin()
    }

    private data class CommandInput(
        val name: String,
        val args: String?
    )

    private data class UpdateUserArgs(
        val userId: Long,
        val relation: String
    )

    private companion object {
        const val COMMAND_PREFIX = "#"
        const val USAGE_CREATE_PROMPT = "用法: #create_prompt <prompt>"
        const val USAGE_UPDATE_USER = "用法: #update_user <user_id> <relation>"
    }

}
