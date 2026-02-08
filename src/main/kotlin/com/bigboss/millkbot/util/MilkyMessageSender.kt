package com.bigboss.millkbot.util

import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.milky.sendPrivateMessage

object MilkyMessageSender {

    suspend fun sendTextAsSegments(milkyClient: MilkyClient, userId: Long, text: String) {
        val segments = MessageTextConverter.parseToOutgoingSegments(text)
        milkyClient.sendPrivateMessage(userId, segments)
    }

    suspend fun sendTextsAsSegments(milkyClient: MilkyClient, userId: Long, texts: List<String>) {
        texts.forEach { text ->
            sendTextAsSegments(milkyClient, userId, text)
        }
    }

    suspend fun sendSegments(milkyClient: MilkyClient, userId: Long, segments: List<OutgoingSegment>) {
        milkyClient.sendPrivateMessage(userId, segments)
    }
}
