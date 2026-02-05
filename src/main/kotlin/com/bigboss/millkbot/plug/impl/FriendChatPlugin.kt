package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.util.MessageTextConverter
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

    override suspend fun handle(msg: IncomingMessage.Friend): Boolean {
        logger.debug("handle message from {}", msg.senderId)

        val content = MessageTextConverter.toTextForFriend(msg.segments)

        milkyClient.sendPrivateMessage(msg.senderId) {
            text(content)
        }

        return true
    }


}