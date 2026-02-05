package com.bigboss.millkbot.plug

import org.ntqqrev.milky.IncomingMessage
import org.springframework.core.Ordered

interface MessagePlugin<T : IncomingMessage> : Ordered {

    /**
     * Handle the message
     * @return true to continue the plugin chain, false to stop
     */
    suspend fun handle(msg: ProcessedMessage<T>): Boolean

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}