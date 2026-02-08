package com.bigboss.millkbot.tool

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class TimeTools {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(description = "获取当前的日期和时间。")
    fun getCurrentTime(): String {
        logger.debug("Tool getCurrentTime called")
        val now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedTime = now.format(formatter)
        val result = "当前时间：$formattedTime"
        logger.debug("Tool getCurrentTime result={}", result)
        return result
    }
}
