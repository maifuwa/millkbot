package com.bigboss.millkbot.schedule

import com.bigboss.millkbot.service.UserService
import com.bigboss.millkbot.util.CronUtil
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Component
class QuartzActuator(
    private val userService: UserService,
    private val scheduleService: ScheduleService
) {
    private val logger = LoggerFactory.getLogger(QuartzActuator::class.java)

    init {
        logger.info("开始初始化定时任务...")

        try {
            logger.info("开始清理过期任务...")
            val expiredCount = scheduleService.deleteExpiredTasks()
            logger.info("清理完成，删除了 $expiredCount 个过期任务")

            logger.info("开始加载启用的定时任务...")
            val enabledTasks = scheduleService.findEnabledTasks()
            logger.info("找到 ${enabledTasks.size} 个启用的定时任务")

            var successCount = 0
            var failCount = 0

            enabledTasks.forEach { task ->
                if (scheduleService.loadTask(task)) {
                    successCount++
                } else {
                    failCount++
                }
            }

            logger.info("定时任务加载完成: 成功 $successCount 个, 失败 $failCount 个")
        } catch (e: Exception) {
            logger.error("初始化定时任务时发生错误", e)
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun generateDailyTasksForMasters() {
        logger.info("开始生成每日定时任务...")

        val masterIds = userService.findMaster()
        if (masterIds.isEmpty()) {
            logger.info("没有找到 Master 用户，跳过任务生成")
            return
        }

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        masterIds.forEach { userId ->
            try {
                generateDailyTasksForUser(userId, today)
                logger.debug("为用户 {} 生成每日任务成功", userId)
            } catch (e: Exception) {
                logger.error("生成每日任务失败", e)
                logger.debug("生成每日任务失败: userId={}", userId)
            }
        }

        logger.info("每日定时任务生成完成，共处理 ${masterIds.size} 个 Master 用户")
    }

    private fun generateDailyTasksForUser(userId: Long, date: String) {
        val morningHour = Random.nextInt(7, 10)
        val morningMinute = Random.nextInt(0, 60)
        createOnceTask(userId, morningHour, morningMinute, date, "早上问候")

        val noonHour = Random.nextInt(12, 14)
        val noonMinute = Random.nextInt(0, 60)
        createOnceTask(userId, noonHour, noonMinute, date, "中文问候")

        val eveningHour = Random.nextInt(18, 21)
        val eveningMinute = Random.nextInt(0, 60)
        createOnceTask(userId, eveningHour, eveningMinute, date, "晚上问候")

        val waterReminders = mutableSetOf<Pair<Int, Int>>()
        while (waterReminders.size < 5) {
            val hour = Random.nextInt(9, 23)
            val minute = Random.nextInt(0, 60)
            waterReminders.add(Pair(hour, minute))
        }

        waterReminders.forEach { (hour, minute) ->
            createOnceTask(userId, hour, minute, date, "提醒用户喝水")
        }
    }

    private fun createOnceTask(userId: Long, hour: Int, minute: Int, date: String, content: String) {
        val dateParts = date.split("-")
        val year = dateParts[0]
        val month = dateParts[1]
        val day = dateParts[2]

        val cronExpr = CronUtil.buildOnceCronExpression(year, month, day, hour, minute)

        scheduleService.createTask(
            cronExpr = cronExpr,
            content = content,
            userId = userId,
            createdBy = "system"
        )
    }
}
