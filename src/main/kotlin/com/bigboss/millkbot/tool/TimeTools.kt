package com.bigboss.millkbot.tool

import com.bigboss.millkbot.util.DateTimeUtil
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service

@Service
class TimeTools {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(description = "获取当前的日期和时间。")
    fun getCurrentTime(): String {
        logger.debug("Tool getCurrentTime called")
        val now = DateTimeUtil.now()
        val formattedTime = DateTimeUtil.formatStandard(now)
        val result = "当前时间：$formattedTime"
        logger.debug("Tool getCurrentTime result={}", result)
        return result
    }
}
