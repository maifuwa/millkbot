package com.bigboss.millkbot.util

import com.bigboss.millkbot.model.User
import org.ntqqrev.milky.*

object MessageTextConverter {

    fun buildChatMessage(user: User): String {
        return buildUserInfoBlock(user)
    }

    fun buildDealChatMessage(user: User, task: String): String {
        return buildString {
            appendLine(buildUserInfoBlock(user))
            appendLine()
            appendLine("[由${user.name}日程触发](agent task)")
            appendLine("taskInfo:")
            appendLine("  content: $task")
        }.trimEnd()
    }

    private fun buildUserInfoBlock(user: User): String {
        val lines = mutableListOf(
            "userInfo:",
            "  id: ${user.id}",
            "  name: ${user.name}",
            "  relation: ${user.relation}",
        )

        user.customPrompt
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { lines.add("  custom prompt: $it") }

        return lines.joinToString("\n")
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
            is IncomingSegment.Face -> faceMap[segment.faceId]?.let { "$it[表情]" } ?: ""
            is IncomingSegment.MarketFace -> segment.summary
            else -> ""
        }
    }

}
