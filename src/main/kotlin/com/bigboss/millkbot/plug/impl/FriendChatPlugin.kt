package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.plug.ProcessedMessage
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.ntqqrev.milky.text
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FriendChatPlugin(
    private val milkyClient: MilkyClient,
    private val agentService: com.bigboss.millkbot.service.AgentService
) : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        val senderId = msg.original.senderId
        val senderName = msg.original.friend.nickname
        logger.debug("handle message from {}", senderId)

        val content = msg.convertedText ?: return false
        val segments = agentService.chat(senderId, senderName, content)

        if (segments.isEmpty()) {
            logger.warn("Agent service returned empty response for user {}", senderId)
            milkyClient.sendPrivateMessage(senderId) {
                text("抱歉，我暂时无法回复您的消息，请稍后再试。")
            }
        } else {
            milkyClient.sendPrivateMessage(senderId, segments)
        }

        return false
    }


}