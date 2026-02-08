package com.bigboss.millkbot.schedule

import com.bigboss.millkbot.service.UserService
import com.bigboss.millkbot.util.DateTimeUtil
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.max
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

        val today = DateTimeUtil.today()

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

    private fun generateDailyTasksForUser(userId: Long, date: LocalDate) {
        val greetings = listOf(
            GreetingSpec("早上问候", hourRange = 7..9),
            GreetingSpec("中文问候", hourRange = 12..13),
            GreetingSpec("晚上问候", hourRange = 18..20),
        )

        greetings.forEach { spec ->
            val time = randomTime(spec.hourRange)
            createOnceTask(userId, date, time.first, time.second, spec.content)
        }

        pickUniqueRandomTimes(count = 5, hourRange = 9..22)
            .forEach { (hour, minute) ->
                createOnceTask(userId, date, hour, minute, "提醒用户喝水")
            }
    }

    private fun createOnceTask(userId: Long, date: LocalDate, hour: Int, minute: Int, content: String) {
        val runAt = date.atTime(hour, minute)
        scheduleService.createTask(
            runAt = runAt,
            content = content,
            userId = userId,
            createdBy = "system"
        )
    }

    private fun randomTime(hourRange: IntRange): Pair<Int, Int> {
        val start = hourRange.first
        val endExclusive = max(hourRange.last + 1, start + 1)
        val hour = Random.nextInt(start, endExclusive)
        val minute = Random.nextInt(0, 60)
        return hour to minute
    }

    private fun pickUniqueRandomTimes(count: Int, hourRange: IntRange): Set<Pair<Int, Int>> {
        val targetCount = max(count, 0)
        val times = mutableSetOf<Pair<Int, Int>>()
        while (times.size < targetCount) {
            times.add(randomTime(hourRange))
        }
        return times
    }

    private data class GreetingSpec(
        val content: String,
        val hourRange: IntRange,
    )
}
