package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.plug.ProcessedMessage
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.ntqqrev.milky.text
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
class FriendCommandPlugin(
    private val milkyClient: MilkyClient
) : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        logger.debug("handle message from {}", msg.original.senderId)

        val content = msg.convertedText ?: return true

        if (!content.startsWith("/")) {
            return true
        }

        milkyClient.sendPrivateMessage(msg.original.senderId) {
            text("$content[command]")
        }

        return false
    }

}