package com.bigboss.millkbot.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

    private val logger = LoggerFactory.getLogger(MilkyService::class.java)
    private var subscribe: Job? = null

    fun startListening() {
        applicationScope.launch {
            milkClient.connectEvent()

            subscribe = launch {
                milkClient.subscribe {
                    if (it is Event.MessageReceive) {
                        when (it.data) {
                            is IncomingMessage.Friend -> logger.info("Received a friend message")
                            else -> logger.info("Received an incoming message")
                        }
                    } else logger.warn("Received event: $it")
                }
            }
        }
    }

    @PreDestroy
    fun stopListening() {
        applicationScope.launch {
            subscribe?.cancel()
            milkClient.disconnectEvent()
        }
    }
}