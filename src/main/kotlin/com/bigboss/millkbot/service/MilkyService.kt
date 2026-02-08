package com.bigboss.millkbot.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.MilkyClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MilkyService(
    private val milkClient: MilkyClient,
    private val applicationScope: CoroutineScope
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var subscribe: Job? = null

    private val _eventFlow = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = 100)
    val eventFlow = _eventFlow.asSharedFlow()

    fun startListening() {
        if (subscribe?.isActive == true) return
        subscribe = applicationScope.launch {
            try {
                milkClient.connectEvent()
                logger.info("Connected to Milky Client")

                milkClient.subscribe { event ->
                    logger.debug("Received event: {}", event.toString())
                    if (event is Event.MessageReceive) {
                        val emitted = _eventFlow.tryEmit(event.data)
                        logger.debug("Message emitted to flow: $emitted")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error in Milky listening", e)
            }
        }
    }

    @PreDestroy
    fun stopListening() {
        subscribe?.cancel()
        applicationScope.launch {
            milkClient.disconnectEvent()
        }
    }
}
