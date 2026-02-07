package com.bigboss.millkbot.util

import com.bigboss.millkbot.model.User
import org.ntqqrev.milky.*

object MessageTextConverter {

    fun buildAgentContextMessage(user: User): String {
        return """
            |userInfo:
            |  id: ${user.id}
            |  name: ${user.name}
            |  relation: ${user.relation}
            |  ${if (user.customPrompt!!.isBlank()) "" else "custom prompt: " + user.customPrompt}
        """.trimMargin()
    }

    private val faceMap: Map<String, String> = FaceEmojiCatalog.faceIdToName

    private val reverseFaceMap: Map<String, String> = faceMap.entries.associate { (k, v) -> v to k }

    private val facePattern = Regex("(/[^/\\[]+)\\[表情]")

    fun parseToOutgoingSegments(message: String): List<OutgoingSegment> {
        val segments = mutableListOf<OutgoingSegment>()
        var lastIndex = 0

        for (match in facePattern.findAll(message)) {
            if (match.range.first > lastIndex) {
                val text = message.substring(lastIndex, match.range.first)
                if (text.isNotEmpty()) {
                    segments.add(OutgoingSegment.Text(OutgoingSegment.Text.Data(text)))
                }
            }
            val faceName = match.groupValues[1]
            val faceId = reverseFaceMap[faceName]
            if (faceId != null) {
                segments.add(OutgoingSegment.Face(OutgoingSegment.Face.Data(faceId)))
            } else {
                segments.add(OutgoingSegment.Text(OutgoingSegment.Text.Data(match.value)))
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < message.length) {
            segments.add(OutgoingSegment.Text(OutgoingSegment.Text.Data(message.substring(lastIndex))))
        }

        return segments.ifEmpty { listOf(OutgoingSegment.Text(OutgoingSegment.Text.Data(message))) }
    }

    fun toTextForFriend(message: List<IncomingSegment>): String {
        return message.joinToString("") { segment ->
            convertSegment(segment)
        }
    }

    private fun convertSegment(segment: IncomingSegment): String {
        return when (segment) {
            is IncomingSegment.Text -> segment.text
            is IncomingSegment.Face -> faceMap[segment.faceId] + "[表情]"
            is IncomingSegment.MarketFace -> segment.summary
            else -> ""
        }
    }

}
