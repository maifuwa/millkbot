package com.bigboss.millkbot.util

import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object CronUtil {

    private val logger = LoggerFactory.getLogger(CronUtil::class.java)

    fun parseCronExpression(cronExpr: String): String {
        return try {
            val parts = cronExpr.trim().split(Regex("\\s+"))

            if (parts.size == 7) {
                val minute = parts[1]
                val hour = parts[2]
                val day = parts[3]
                val month = parts[4]
                val year = parts[6]

                val sb = StringBuilder()

                if (year != "*" && year != "?") {
                    sb.append("${year}年")
                }

                if (month != "*" && month != "?") {
                    sb.append("${month}月")
                }

                if (day != "*" && day != "?") {
                    sb.append("${day}日")
                }

                if (hour != "*" && minute != "*") {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append("${hour}:${minute.padStart(2, '0')}")
                } else if (hour != "*") {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append("${hour}时")
                } else if (minute != "*") {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append("每小时${minute}分")
                }

                if (sb.isEmpty()) {
                    return cronExpr
                }

                return sb.toString()
            }

            cronExpr
        } catch (e: Exception) {
            logger.warn("Failed to parse cron expression: {}", cronExpr, e)
            cronExpr
        }
    }

    fun isTaskExpired(cronExpr: String, now: LocalDateTime): Boolean {
        return try {
            val parts = cronExpr.trim().split(Regex("\\s+"))

            if (parts.size == 7) {
                val year = parts[6].toIntOrNull() ?: return false
                val month = parts[4].toIntOrNull() ?: return false
                val day = parts[3].toIntOrNull() ?: return false
                val hour = parts[2].toIntOrNull() ?: return false
                val minute = parts[1].toIntOrNull() ?: return false

                val taskDateTime = LocalDateTime.of(year, month, day, hour, minute)

                return taskDateTime.isBefore(now)
            }

            false
        } catch (e: Exception) {
            logger.warn("Failed to check if task is expired: {}", cronExpr, e)
            false
        }
    }


    fun buildOnceCronExpression(year: String, month: String, day: String, hour: Int, minute: Int): String {
        return "0 $minute $hour $day $month ? $year"
    }
}
