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
    private val milkyClient: MilkyClient
) : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        logger.debug("handle message from {}", msg.original.senderId)

        val content = msg.convertedText ?: return false

        milkyClient.sendPrivateMessage(msg.original.senderId) {
            text(content)
        }

        return false
    }


}