package com.bigboss.millkbot.plug

import org.ntqqrev.milky.IncomingMessage
import org.springframework.core.Ordered

interface MessagePlugin<T : IncomingMessage> : Ordered {

    suspend fun handle(msg: T): Boolean

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}