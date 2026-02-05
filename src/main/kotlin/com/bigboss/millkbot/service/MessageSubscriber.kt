package com.bigboss.millkbot.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ntqqrev.milky.IncomingMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MessageSubscriber(
    private val milkyService: MilkyService,
    private val applicationScope: CoroutineScope
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        applicationScope.launch {
            milkyService.eventFlow.collect { message ->
                when (message) {
                    is IncomingMessage.Friend -> handleFriendMessage(message)
                    is IncomingMessage.Group -> handleGroupMessage(message)
                    else -> logger.debug("Other message type received")
                }
            }
        }
    }

    private fun handleFriendMessage(msg: IncomingMessage.Friend) {
        logger.debug("Received friend message")
    }

    private fun handleGroupMessage(msg: IncomingMessage.Group) {
        logger.debug("Received group message")
    }
}