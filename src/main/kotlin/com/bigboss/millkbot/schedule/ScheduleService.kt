package com.bigboss.millkbot.schedule

import com.bigboss.millkbot.model.ScheduledTask
import com.bigboss.millkbot.model.enabled
import com.bigboss.millkbot.model.id
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.createUpdate
import org.babyfish.jimmer.sql.kt.findById
import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduleService(
    private val sqlClient: KSqlClient,
    private val scheduler: Scheduler
) {

    private val logger = LoggerFactory.getLogger(ScheduleService::class.java)

    @Transactional
    fun createTask(cronExpr: String, content: String, userId: Long, createdBy: String): Boolean {
        val task = sqlClient.save(ScheduledTask {
            this.cronExpr = cronExpr
            this.content = content
            this.createdBy = createdBy
            this.userId = userId
        }, SaveMode.INSERT_ONLY).modifiedEntity

        return try {
            if (!scheduler.isStarted) scheduler.start()

            val jobKey = JobKey.jobKey(task.id.toString(), task.createdBy)
            val triggerKey = TriggerKey.triggerKey(task.id.toString(), task.createdBy)

            if (scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)) {
                true
            } else {
                val jobDetail = JobBuilder.newJob(AgentTask::class.java)
                    .withIdentity(task.id.toString(), task.createdBy)
                    .usingJobData("userId", task.user.id)
                    .usingJobData("taskId", task.id)
                    .usingJobData("createdBy", task.createdBy)
                    .build()

                val trigger = TriggerBuilder.newTrigger()
                    .withIdentity(task.id.toString(), task.createdBy)
                    .withSchedule(CronScheduleBuilder.cronSchedule(task.cronExpr))
                    .build()

                scheduler.scheduleJob(jobDetail, trigger)
                true
            }
        } catch (e: Exception) {
            logger.error("Failed to schedule task id=${task.id}, cron=${task.cronExpr}", e)
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
        logger.error("Failed to delete task id=${task.id}, group=${task.createdBy}", e)
        false
    }

    fun findTaskById(id: Long): ScheduledTask? {
        return sqlClient.findById(id)
    }

    private fun disableTask(taskId: Long): Boolean {
        return sqlClient.createUpdate {
            set(table.enabled, false)
            where(table.id eq taskId)
        }.execute() > 0
    }
}
