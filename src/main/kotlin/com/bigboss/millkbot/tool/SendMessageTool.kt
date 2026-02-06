package com.bigboss.millkbot.tool

import com.bigboss.millkbot.util.MessageTextConverter
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

class SendMessageTool(
    private val milkyClient: MilkyClient,
    private val senderId: Long,
) {

    private var sendCount = 0

    @Tool(
        description = """Send a QQ message to the user. Each call sends one message.
        You can call this tool at most 3 times per reply.Usually 1-2 times is sufficient.
        You can optionally embed face expressions using /表情名[表情], e.g. /微笑[表情]. Face expressions are NOT required — only use them when they naturally fit the context. Most messages should be plain text.
        Available faces: /惊讶, /撇嘴, /色, /发呆, /得意, /流泪, /害羞, /闭嘴, /睡, /大哭, /尴尬, /发怒, /调皮, /呲牙, /微笑, /难过, /酷, /抓狂, /吐, /偷笑, /可爱, /白眼, /傲慢, /饥饿, /困, /惊恐, /流汗, /憨笑, /悠闲, /奋斗, /咒骂, /疑问, /嘘, /晕, /疯了, /衰, /骷髅, /敲打, /再见, /蛋糕, /咖啡, /玫瑰, /爱心, /太阳, /月亮, /赞, /踩, /握手, /胜利, /飞吻, /怄火, /西瓜, /冷汗, /擦汗, /抠鼻, /鼓掌, /糗大了, /坏笑, /左哼哼, /右哼哼, /哈欠, /鄙视, /委屈, /快哭了, /阴险, /亲亲, /吓, /可怜, /菜刀, /啤酒, /篮球, /乒乓, /示爱, /瓢虫, /抱拳, /勾引, /笑哭, /我最美, /猪头, /红包, /捂脸, /耶, /奸笑, /机智, /皱眉, /暗中观察, /加油, /吃瓜, /无眼笑, /敬礼, /裂开, /加油加油, /脑阔疼, /沧桑, /悠哉, /打脸, /哦, /睁眼, /敲开心, /汪汪, /汗, /打call, /变形, /嗑瓜子, /惊喜, /生气, /喝彩, /天啊, /Emm, /社会社会, /旺柴, /好的, /打球, /怪我咯, /喵喵, /求红包, /谢谢老板, /略略略"""
    )
    fun sendMessage(
        @ToolParam(description = "The message content to send. Can contain face expressions like /微笑[表情]")
        message: String,
    ): String {
        if (sendCount >= MAX_MESSAGES) {
            return "Message limit reached. Cannot send more messages."
        }
        val segments = MessageTextConverter.parseToOutgoingSegments(message)
        runBlocking {
            milkyClient.sendPrivateMessage(senderId, segments)
        }
        sendCount++
        return "Message sent successfully ($sendCount/$MAX_MESSAGES)"
    }

    companion object {
        private const val MAX_MESSAGES = 3
    }
}
