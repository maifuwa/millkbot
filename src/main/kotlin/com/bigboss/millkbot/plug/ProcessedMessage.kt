package com.bigboss.millkbot.plug

import org.ntqqrev.milky.IncomingMessage

data class ProcessedMessage<T : IncomingMessage>(
    val original: T,
    var convertedText: String? = null
)
