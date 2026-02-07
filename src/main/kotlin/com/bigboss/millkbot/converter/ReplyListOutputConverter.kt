package com.bigboss.millkbot.converter

import com.bigboss.millkbot.util.FaceEmojiCatalog
import org.springframework.ai.converter.ListOutputConverter

class ReplyListOutputConverter : ListOutputConverter() {

    override fun getFormat(): String {
        return """
            |你必须遵守以下回复规则：
            |1. 通常使用1-2次即可完成回复，尽量不要使用3次。
            |2. 表情不是必需的，大多数消息应该是纯文本。可以适当使用表情来增强语气，但每条消息最多使用1-2个表情，保持优雅克制。
            |3. 表情必须使用 /表情名[表情] 格式，例如 /微笑[表情]。
            |4. 可用表情：${FaceEmojiCatalog.supportedEmojis.joinToString(", ")}
            |
            |输出格式要求：
            |${super.getFormat()}
        """.trimMargin()
    }
}
