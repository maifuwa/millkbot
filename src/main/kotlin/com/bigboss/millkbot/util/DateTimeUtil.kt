package com.bigboss.millkbot.util

import org.slf4j.LoggerFactory
import java.time.LocalDate
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

    private val STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun formatForDisplay(dateTime: LocalDateTime): String {
        return try {
            dateTime.format(DISPLAY_FORMATTER)
        } catch (e: Exception) {
            logger.warn("Failed to format datetime: {}", dateTime, e)
            dateTime.toString()
        }
    }

    fun formatStandard(dateTime: LocalDateTime): String {
        return try {
            dateTime.format(STANDARD_FORMATTER)
        } catch (e: Exception) {
            logger.warn("Failed to format datetime: {}", dateTime, e)
            dateTime.toString()
        }
    }

    fun parseStandardOrNull(text: String): LocalDateTime? {
        val source = text.trim()
        if (source.isBlank()) return null

        return runCatching {
            LocalDateTime.parse(source, STANDARD_FORMATTER)
        }.getOrElse {
            logger.warn("Failed to parse datetime: {}", text, it)
            null
        }
    }

    fun isTaskExpired(runAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now(ZONE_ID)): Boolean {
        return runAt.isBefore(now)
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now(ZONE_ID)
    }

    fun today(): LocalDate {
        return LocalDate.now(ZONE_ID)
    }

    fun getZoneId(): ZoneId {
        return ZONE_ID
    }
}
