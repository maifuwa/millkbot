package com.bigboss.millkbot.util

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtil {

    private val logger = LoggerFactory.getLogger(DateTimeUtil::class.java)

    private val ZONE_ID: ZoneId = try {
        val timeZone = System.getenv("TZ") ?: "Asia/Shanghai"
        ZoneId.of(timeZone).also {
            logger.info("Using timezone: $timeZone")
        }
    } catch (e: Exception) {
        logger.warn("Failed to parse timezone from environment variable, using Asia/Shanghai as default", e)
        ZoneId.of("Asia/Shanghai")
    }

    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")

    fun formatForDisplay(dateTime: LocalDateTime): String {
        return try {
            dateTime.format(DISPLAY_FORMATTER)
        } catch (e: Exception) {
            logger.warn("Failed to format datetime: {}", dateTime, e)
            dateTime.toString()
        }
    }

    fun isTaskExpired(runAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now(ZONE_ID)): Boolean {
        return runAt.isBefore(now)
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now(ZONE_ID)
    }

    fun getZoneId(): ZoneId {
        return ZONE_ID
    }
}
