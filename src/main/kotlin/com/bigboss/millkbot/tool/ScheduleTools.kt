package com.bigboss.millkbot.tool

import com.bigboss.millkbot.schedule.ScheduleService
import com.bigboss.millkbot.util.DateTimeUtil
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduleTools(private val scheduleService: ScheduleService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(description = "获取用户的日程安排列表。返回用户所有启用的定时任务，包括执行时间（可读格式）和任务内容。当用户询问\"我的日程\"、\"我有什么安排\"、\"查看我的任务\"等问题时使用此工具。")
    fun getUserSchedule(request: UserIdRequest): String {
        logger.debug("Tool getUserSchedule called: userId={}", request.userId)

        return try {
            val tasks = scheduleService.findEnableTaskByUser(request.userId)

            if (tasks.isEmpty()) {
                logger.debug("Tool getUserSchedule result: no tasks for userId={}", request.userId)
                return "您当前没有任何日程安排。"
            }

            val taskList = tasks.mapIndexed { index, task ->
                "${index + 1}. ${DateTimeUtil.formatForDisplay(task.runAt)} - ${task.content}"
            }.joinToString("\n")

            val result = """
                您的日程安排如下：

                $taskList
            """.trimIndent()

            logger.debug("Tool getUserSchedule result: found {} tasks for userId={}", tasks.size, request.userId)
            result
        } catch (e: Exception) {
            logger.error("Tool getUserSchedule failed: userId={} error={}", request.userId, e.message, e)
            "获取日程失败: ${e.message}"
        }
    }

    @Tool(description = "创建用户的日程安排。用于为用户创建一次性的定时任务/日程提醒。当用户说\"提醒我...\"、\"帮我创建一个日程\"、\"明天X点提醒我...\"等时使用此工具。")
    fun createUserSchedule(request: CreateScheduleRequest): String {
        logger.debug(
            "Tool createUserSchedule called: userId={}, time={}-{}-{} {}:{}, content={}",
            request.userId, request.year, request.month, request.day, request.hour, request.minute, request.content
        )

        return try {
            if (request.month !in 1..12) {
                return "创建日程失败：月份必须在 1-12 之间"
            }
            if (request.day !in 1..31) {
                return "创建日程失败：日期必须在 1-31 之间"
            }
            if (request.hour !in 0..23) {
                return "创建日程失败：小时必须在 0-23 之间"
            }
            if (request.minute !in 0..59) {
                return "创建日程失败：分钟必须在 0-59 之间"
            }
            if (request.content.isBlank()) {
                return "创建日程失败：日程内容不能为空"
            }

            val runAt = LocalDateTime.of(
                request.year,
                request.month,
                request.day,
                request.hour,
                request.minute
            )

            val success = scheduleService.createTask(
                runAt = runAt,
                content = request.content,
                userId = request.userId,
                createdBy = "user"
            )

            val result = if (success) {
                val timeStr = DateTimeUtil.formatForDisplay(runAt)
                "日程创建成功！\n时间：$timeStr\n内容：${request.content}"
            } else {
                "创建日程失败，请稍后重试"
            }

            logger.debug("Tool createUserSchedule result: success={}", success)
            result
        } catch (e: Exception) {
            logger.error(
                "Tool createUserSchedule failed: userId={} error={}",
                request.userId, e.message, e
            )
            "创建日程失败: ${e.message}"
        }
    }

    @Serializable
    data class UserIdRequest(
        val userId: Long
    )

    @Serializable
    data class CreateScheduleRequest(
        val userId: Long,

        val year: Int,

        val month: Int,

        val day: Int,

        val hour: Int,

        val minute: Int,

        val content: String
    )
}
