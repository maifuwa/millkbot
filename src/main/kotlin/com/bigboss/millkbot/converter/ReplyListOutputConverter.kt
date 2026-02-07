package com.bigboss.millkbot.converter

import com.bigboss.millkbot.util.FaceEmojiCatalog
import org.springframework.ai.converter.ListOutputConverter

class ReplyListOutputConverter : ListOutputConverter() {

    override fun getFormat(): String {
        return """
            |你必须遵守以下回复规则：
            |1. 回复次数优先级：
            |   - 能1条说清就只发1条。
            |   - 需要补充不同信息点时再发第2条。
            |   - 仅在确有必要时才超过2条。
            |2. 回复内容要求：
            |   - 每条都要是完整、自然、可直接发送的话，不要拆成碎句凑条数。
            |   - 先直接回答用户核心问题，再给简短补充；避免空话、重复和模板腔。
            |   - 单条可包含多句，不要把一个完整意思拆到多条里。
            |3. 表情不是必需的，大多数消息应为纯文本。可少量使用表情增强语气，但每条最多1-2个，保持克制。
            |4. 表情必须使用 /表情名[表情] 格式，例如 /微笑[表情]。
            |5. 可用表情：${FaceEmojiCatalog.supportedEmojis.joinToString(", ")}
            |
            |输出格式要求：
            |请仅输出一个 JSON 数组（List<String>），不要输出任何额外说明。
            |数组中的每个 string 元素表示一次发送的一条消息。
            |数组长度尽量短。
            |按发送顺序填写回复内容，例如：["你好", "我在，有什么可以帮你？"]
        """.trimMargin()
    }
}
