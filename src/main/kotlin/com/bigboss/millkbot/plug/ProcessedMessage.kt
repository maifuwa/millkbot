package com.bigboss.millkbot.plug

import com.bigboss.millkbot.model.User
import org.ntqqrev.milky.IncomingMessage

data class ProcessedMessage<T : IncomingMessage>(
    val original: T,
    var convertedText: String? = null,
    var user: User? = null,
)
