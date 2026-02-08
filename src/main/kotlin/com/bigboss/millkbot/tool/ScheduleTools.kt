package com.bigboss.millkbot.tool

import com.bigboss.millkbot.model.ScheduledTask
import com.bigboss.millkbot.schedule.ScheduleService
import com.bigboss.millkbot.util.DateTimeUtil
import com.bigboss.millkbot.util.JsonUtil
import com.bigboss.millkbot.util.encode
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service

@Service
class ScheduleTools(private val scheduleService: ScheduleService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(
        description = """
        获取用户的日程安排列表。
        返回用户所有启用的定时任务的JSON数组，包括任务ID、执行时间、任务内容、创建者和启用状态。

        返回格式：JSON数组，每个元素包含：
        - id: 任务ID
        - runAt: 执行时间（LocalDateTime）
        - content: 任务内容
        - createdBy: 创建者
        - enabled: 是否启用
    """
    )
    fun getUserSchedule(userId: Long): String {
        logger.debug("Tool getUserSchedule called: userId={}", userId)

        return try {
            val tasks = scheduleService.findEnableTaskByUser(userId)

            logger.debug("Tool getUserSchedule result: found {} tasks for userId={}", tasks.size, userId)
            JsonUtil.encode(tasks)
        } catch (e: Exception) {
            logger.error("Tool getUserSchedule failed: userId={} error={}", userId, e.message, e)
            JsonUtil.encode(emptyList<ScheduledTask>())
        }
    }

    @Tool(
        description = """
        创建用户的日程安排。

        参数说明：
        - userId: userInfo.id
        - runAtStr: 执行时间，格式为 "yyyy-MM-dd HH:mm:ss"
        - content: 日程内容，格式为 “动词(+名词)”，比如：“打招呼”、“提醒喝水”
    """
    )
    fun createUserSchedule(
        userId: Long,
        runAtStr: String,
        content: String
    ): String {
        logger.debug("""Tool createUserSchedule called: userId=$userId, runAtStr=$runAtStr, content=$content""")

        return try {
            if (content.isBlank()) {
                logger.warn("Empty content")
                return "false"
            }

            val runAt = DateTimeUtil.parseStandardOrNull(runAtStr) ?: return "false"

            val success = scheduleService.createTask(
                runAt = runAt,
                content = content,
                userId = userId,
                createdBy = "user"
            )

            logger.debug("Tool createUserSchedule result: success=$success")
            success.toString()
        } catch (e: Exception) {
            logger.error("Tool createUserSchedule failed: userId=$userId error=${e.message}", e)
            "false"
        }
    }
}
