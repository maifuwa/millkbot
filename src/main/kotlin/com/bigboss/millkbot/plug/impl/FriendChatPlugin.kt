package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.plug.ProcessedMessage
import com.bigboss.millkbot.service.AgentService
import com.bigboss.millkbot.util.MessageTextConverter
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.ntqqrev.milky.text
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FriendChatPlugin(
    private val milkyClient: MilkyClient,
    private val agentService: AgentService,
) : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        val senderId = msg.original.senderId
        logger.debug("handle message from {}", senderId)

        val content = msg.convertedText ?: return false
        val user = msg.user ?: run {
            logger.warn("Missing user context for sender {}", senderId)
            return false
        }

        try {
            val replies = agentService.chat(user, content)
            if (replies.isNotEmpty()) {
                replies.forEach { reply ->
                    val segments = MessageTextConverter.parseToOutgoingSegments(reply)
                    milkyClient.sendPrivateMessage(senderId, segments)
                }
            }
        } catch (e: Exception) {
            logger.error("Agent service error for user {}", senderId, e)
            milkyClient.sendPrivateMessage(senderId) {
                text("抱歉，我暂时无法回复您的消息，请稍后再试。")
            }
        }

        return false
    }
}
