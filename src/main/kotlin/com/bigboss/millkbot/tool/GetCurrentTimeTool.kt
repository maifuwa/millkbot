package com.bigboss.millkbot.tool

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GetCurrentTimeTool {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(
        description = """获取当前的日期和时间（Asia/Shanghai 时区）。
        返回易读的时间格式，包括年、月、日、时、分、秒。
        当你需要知道现在是什么时间时使用此工具。"""
    )
    fun getCurrentTime(): String {
        logger.debug("Tool GetCurrentTimeTool.getCurrentTime called")
        val now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedTime = now.format(formatter)
        val result = "当前时间：$formattedTime"
        logger.debug("Tool GetCurrentTimeTool.getCurrentTime result={}", result)
        return result
    }
}
