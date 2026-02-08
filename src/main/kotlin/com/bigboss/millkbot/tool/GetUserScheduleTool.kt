package com.bigboss.millkbot.tool

import com.bigboss.millkbot.schedule.ScheduleService
import com.bigboss.millkbot.util.CronUtil
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component

@Component
class GetUserScheduleTool(
    private val scheduleService: ScheduleService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(
        description = """获取用户的日程安排列表。
        返回用户所有启用的定时任务，包括执行时间（可读格式）和任务内容。
        当用户询问"我的日程"、"我有什么安排"、"查看我的任务"等问题时使用此工具。"""
    )
    fun getUserSchedule(userId: Long): String {
        logger.debug("Tool GetUserScheduleTool.getUserSchedule called: userId={}", userId)

        return try {
            val tasks = scheduleService.findEnableTaskByUser(userId)

            if (tasks.isEmpty()) {
                val result = "您当前没有任何日程安排。"
                logger.debug("Tool GetUserScheduleTool.getUserSchedule result: no tasks for userId={}", userId)
                return result
            }

            val taskList = tasks.mapIndexed { index, task ->
                "${index + 1}. ${CronUtil.parseCronExpression(task.cronExpr)} - ${task.content}"
            }.joinToString("\n")

            val result = """
                您的日程安排如下：

                $taskList
            """.trimIndent()

            logger.debug("Tool GetUserScheduleTool.getUserSchedule result: found {} tasks for userId={}", tasks.size, userId)
            result
        } catch (e: Exception) {
            logger.error("Tool GetUserScheduleTool.getUserSchedule failed: userId={} error={}", userId, e.message, e)
            "获取日程失败: ${e.message}"
        }
    }
}
