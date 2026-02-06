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

        val content = msg.convertedText ?: return true

        if (!content.startsWith("#")) {
            return true
        }

        val senderId = msg.original.senderId
        val parts = content.substring(1).split(" ", limit = 2)
        val command = parts[0]
        val args = parts.getOrNull(1)

        val response = when (command) {
            "create_master" -> handleCreateMaster(senderId)
            "create_prompt" -> handleCreatePrompt(senderId, args)
            "update_master" -> handleUpdateMaster(senderId, args)
            "all" -> handleAll()
            else -> "未知命令，请使用 #all 查看所有可用命令"
        }

        milkyClient.sendPrivateMessage(senderId) {
            text(response)
        }

        return false
    }

    private fun handleCreateMaster(senderId: Long): String {
        return if (userService.createMaster(senderId)) {
            "成功认主"
        } else {
            "认主失败"
        }
    }

    private fun handleCreatePrompt(senderId: Long, prompt: String?): String {
        if (prompt.isNullOrBlank()) {
            return "用法: #create_prompt <prompt>"
        }
        return if (userService.createPrompt(senderId, prompt)) {
            "更新 prompt 成功"
        } else {
            "更新 prompt 失败"
        }
    }

    private fun handleUpdateMaster(senderId: Long, newMasterIdStr: String?): String {
        if (newMasterIdStr.isNullOrBlank()) {
            return "用法: #update_master <Id>"
        }
        val newMasterId = newMasterIdStr.toLongOrNull()
            ?: return "无效的用户ID: $newMasterIdStr"

        val user = userService.getUser(senderId, "")
        return if (userService.updateMaster(user, newMasterId)) {
            "添加成功"
        } else {
            "添加失败"
        }
    }

    private fun handleAll(): String {
        return """
            可用命令列表:

            #create_master
            - 认主仪式

            #create_prompt <prompt>
            - 更新 prompt

            #update_master <Id>
            - 家庭共享

            #all
            - 查看所有可用命令
        """.trimIndent()
    }

}