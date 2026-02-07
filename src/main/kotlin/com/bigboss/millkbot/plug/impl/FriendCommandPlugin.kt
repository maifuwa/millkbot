package com.bigboss.millkbot.plug.impl

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

        if (!content.startsWith("#")) {
            return true
        }

        val senderId = msg.original.senderId
        val commandLine = content.substring(1).trim()
        if (commandLine.isBlank()) {
            milkyClient.sendPrivateMessage(senderId) {
                text(handleAll())
            }
            return false
        }

        val parts = commandLine.split(Regex("\\s+"), limit = 2)
        val command = parts[0]
        val args = parts.getOrNull(1)

        val response = when (command) {
            "create_master" -> handleCreateMaster(senderId)
            "create_prompt" -> handleCreatePrompt(senderId, args)
            "update_user" -> handleUpdateUser(senderId, args)
            "all", "help" -> handleAll()
            else -> "未知命令，请使用 #all 查看所有可用命令"
        }

        milkyClient.sendPrivateMessage(senderId) {
            text(response)
        }

        return false
    }

    private fun handleCreateMaster(senderId: Long): String {
        userService.getUser(senderId, "")
        return if (userService.createMaster(senderId)) {
            "创建 Master 用户成功"
        } else {
            "创建 Master 用户失败"
        }
    }

    private fun handleCreatePrompt(senderId: Long, prompt: String?): String {
        if (prompt.isNullOrBlank()) {
            return "用法: #create_prompt <prompt>"
        }
        userService.getUser(senderId, "")
        return if (userService.createPrompt(senderId, prompt)) {
            "设置自定义提示词成功"
        } else {
            "设置自定义提示词失败"
        }
    }

    private fun handleUpdateUser(senderId: Long, updateArgs: String?): String {
        if (updateArgs.isNullOrBlank()) {
            return "用法: #update_user <user_id> <relation>"
        }

        val parts = updateArgs.trim().split(Regex("\\s+"), limit = 2)
        if (parts.size < 2) {
            return "用法: #update_user <user_id> <relation>"
        }

        val targetUserId = parts[0].toLongOrNull()
            ?: return "无效的用户ID: ${parts[0]}"

        val relation = parts[1].trim()
        if (relation.isBlank()) {
            return "用法: #update_user <user_id> <relation>"
        }

        val user = userService.getUser(senderId, "")
        return if (userService.updateUserRelation(user, targetUserId, relation)) {
            "更新用户关系成功"
        } else {
            "更新用户关系失败"
        }
    }

    private fun handleAll(): String {
        return """
           |可用命令列表:
           |1. #create_master - 创建 master 用户
           |2. #create_prompt <prompt> - 设置自定义提示词
           |3. #update_user <user_id> <relation> - 修改用户关系
           |4. #all - 查看所有命令
        """.trimMargin()
    }

}
