package com.bigboss.millkbot.plug.impl

import com.bigboss.millkbot.plug.MessagePlugin
import com.bigboss.millkbot.plug.ProcessedMessage
import com.bigboss.millkbot.util.MessageTextConverter
import org.ntqqrev.milky.IncomingMessage
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
class MessageConverterPlugin : MessagePlugin<IncomingMessage.Friend> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override suspend fun handle(msg: ProcessedMessage<IncomingMessage.Friend>): Boolean {
        logger.debug("Converting message from {}", msg.original.senderId)
        msg.convertedText = MessageTextConverter.toTextForFriend(msg.original.segments)
        return true
    }
}
