package com.bigboss.millkbot.service

import com.bigboss.millkbot.plug.MessagePlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ntqqrev.milky.IncomingMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MessageSubscriber(
    private val milkyService: MilkyService,
    private val allPlugins: List<MessagePlugin<out IncomingMessage>>,
    private val applicationScope: CoroutineScope
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val friendPlugins by lazy {
        allPlugins.filterIsInstance<MessagePlugin<IncomingMessage.Friend>>()
    }

    private val groupPlugins by lazy {
        allPlugins.filterIsInstance<MessagePlugin<IncomingMessage.Group>>()
    }

    init {
        logger.info("MessageSubscriber initialized, starting to collect events")
        applicationScope.launch {
            milkyService.eventFlow.collect { message ->
                when (message) {
                    is IncomingMessage.Friend -> executeChain(message, friendPlugins)
                    is IncomingMessage.Group -> executeChain(message, groupPlugins)
                    else -> logger.debug("Other message type received")
                }
            }
        }
    }

    private suspend fun <T : IncomingMessage> executeChain(msg: T, plugins: List<MessagePlugin<T>>) {
        for (plugin in plugins) {
            try {
                if (plugin.handle(msg)) {
                    logger.debug("Chain broken by: ${plugin.javaClass.simpleName}")
                    return
                }
            } catch (e: Exception) {
                logger.error("Error in plugin ${plugin.javaClass.simpleName}: ${e.message}")
            }
        }
    }
}