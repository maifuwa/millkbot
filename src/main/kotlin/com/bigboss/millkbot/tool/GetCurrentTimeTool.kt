package com.bigboss.millkbot.tool

import org.springframework.ai.tool.annotation.Tool
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GetCurrentTimeTool {

    @Tool(
        description = """Get the current date and time in China (Asia/Shanghai timezone).
        Returns the current time in a human-readable format including year, month, day, hour, minute, and second.
        Use this tool when you need to know what time it is now."""
    )
    fun getCurrentTime(): String {
        val now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedTime = now.format(formatter)
        return "Current time in China: $formattedTime"
    }
}
