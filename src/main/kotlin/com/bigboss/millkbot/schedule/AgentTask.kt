package com.bigboss.millkbot.schedule

import com.bigboss.millkbot.model.ScheduledTask
import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.service.AgentService
import com.bigboss.millkbot.service.UserService
import com.bigboss.millkbot.util.MessageTextConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ntqqrev.milky.MilkyClient
import org.ntqqrev.milky.sendPrivateMessage
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
class AgentTask(
    private val userService: UserService,
    private val scheduleService: ScheduleService,
    private val agentService: AgentService,
    private val milkyClient: MilkyClient,
    private val applicationScope: CoroutineScope
) : QuartzJobBean() {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    override fun executeInternal(context: JobExecutionContext) {
        val (userId, taskId) = extractTaskParameters(context)

        val user = userService.findUserById(userId)!!
        val task = scheduleService.findTaskById(taskId)!!

        logger.info("Quartz task fire: userId={}, taskId={}, createdBy={}", userId, taskId, task.createdBy)

        if (task.enabled) {
            processAndSendTask(user, task)
        }

        scheduleService.deleteTask(task)
    }

    private fun processAndSendTask(user: User, task: ScheduledTask) {
        try {
            applicationScope.launch {
                val replies = agentService.deal(user, task.content)
                replies.forEach { reply ->
                    val segments = MessageTextConverter.parseToOutgoingSegments(reply)
                    milkyClient.sendPrivateMessage(user.id, segments)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to process and send scheduled task, userId={}, taskId={}", user.id, task.id, e)
        }
    }

    private fun extractTaskParameters(context: JobExecutionContext): TaskParameters {
        val taskMap = context.jobDetail.jobDataMap
        return TaskParameters(
            userId = taskMap.getLong("userId"),
            taskId = taskMap.getLong("taskId"),
        )
    }

    private data class TaskParameters(
        val userId: Long,
        val taskId: Long,
    )
}
