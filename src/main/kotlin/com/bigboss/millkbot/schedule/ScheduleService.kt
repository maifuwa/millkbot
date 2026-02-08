package com.bigboss.millkbot.schedule

import com.bigboss.millkbot.model.*
import com.bigboss.millkbot.util.DateTimeUtil
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.createUpdate
import org.babyfish.jimmer.sql.kt.findById
import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date

@Service
class ScheduleService(
    private val sqlClient: KSqlClient,
    private val scheduler: Scheduler
) {

    private val logger = LoggerFactory.getLogger(ScheduleService::class.java)

    @Transactional
    fun createTask(runAt: LocalDateTime, content: String, userId: Long, createdBy: String): Boolean {
        val task = sqlClient.save(ScheduledTask {
            this.runAt = runAt
            this.content = content
            this.createdBy = createdBy
            this.userId = userId
        }, SaveMode.INSERT_ONLY).modifiedEntity

        return try {
            val jobKey = JobKey.jobKey(task.id.toString(), task.createdBy)
            val triggerKey = TriggerKey.triggerKey(task.id.toString(), task.createdBy)

            if (scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)) {
                true
            } else {
                startTask(task)
            }
        } catch (e: Exception) {
            logger.error("Failed to schedule task id=${task.id}, runAt=${task.runAt}", e)
            false
        }
    }

    @Transactional
    fun deleteTask(task: ScheduledTask): Boolean = try {
        if (!disableTask(task.id)) return false

        val jobKey = JobKey.jobKey(task.id.toString(), task.createdBy)
        val triggerKey = TriggerKey.triggerKey(task.id.toString(), task.createdBy)

        when {
            !scheduler.checkExists(jobKey) && !scheduler.checkExists(triggerKey) -> true
            else -> scheduler.unscheduleJob(triggerKey) && scheduler.deleteJob(jobKey)
        }
    } catch (e: Exception) {
        logger.error("Failed to delete task id=${task.id}", e)
        logger.debug("Failed to delete task details: taskId={}, group={}", task.id, task.createdBy)
        false
    }

    fun findTaskById(id: Long): ScheduledTask? {
        return sqlClient.findById(id)
    }

    fun findEnabledTasks(): List<ScheduledTask> {
        return sqlClient.createQuery(ScheduledTask::class) {
            where(table.enabled eq true)
            select(table.fetchBy {
                allScalarFields()
                user {
                    allScalarFields()
                }
            })
        }.execute()
    }

    @Transactional
    fun deleteExpiredTasks(): Int {
        val allTasks = sqlClient.createQuery(ScheduledTask::class) {
            where(table.enabled eq true)
            select(table.fetchBy {
                allScalarFields()
            })
        }.execute()

        var deletedCount = 0
        val now = DateTimeUtil.now()

        allTasks.forEach { task ->
            try {
                if (DateTimeUtil.isTaskExpired(task.runAt, now)) {
                    if (deleteTask(task)) {
                        deletedCount++
                        logger.info("删除过期任务: id=${task.id}, runAt=${task.runAt}")
                    }
                }
            } catch (e: Exception) {
                logger.error("检查任务是否过期时出错: id=${task.id}", e)
            }
        }

        return deletedCount
    }

    fun loadTask(task: ScheduledTask): Boolean {
        return try {
            val jobKey = JobKey.jobKey(task.id.toString(), task.createdBy)
            val triggerKey = TriggerKey.triggerKey(task.id.toString(), task.createdBy)

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey)
            }
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey)
            }

            startTask(task)
            logger.info("Successfully loaded task id=${task.id}, runAt=${task.runAt}")
            true
        } catch (e: Exception) {
            logger.error("Failed to load task id=${task.id}, runAt=${task.runAt}", e)
            false
        }
    }

    private fun startTask(task: ScheduledTask): Boolean {
        if (!scheduler.isStarted) scheduler.start()

        val jobDetail = JobBuilder.newJob(AgentTask::class.java)
            .withIdentity(task.id.toString(), task.createdBy)
            .usingJobData("userId", task.user.id)
            .usingJobData("taskId", task.id)
            .usingJobData("createdBy", task.createdBy)
            .build()

        val startTime = Date.from(
            task.runAt.atZone(DateTimeUtil.getZoneId()).toInstant()
        )

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(task.id.toString(), task.createdBy)
            .startAt(startTime)
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
        return true
    }

    private fun disableTask(taskId: Long): Boolean {
        return sqlClient.createUpdate {
            set(table.enabled, false)
            where(table.id eq taskId)
        }.execute() > 0
    }

    fun findEnableTaskByUser(id: Long): List<ScheduledTask> {
        return sqlClient.createQuery(ScheduledTask::class) {
            where(table.userId eq id)
            where(table.createdBy eq "user")
            where(table.enabled eq true)
            select(table)
        }.execute()
    }
}
