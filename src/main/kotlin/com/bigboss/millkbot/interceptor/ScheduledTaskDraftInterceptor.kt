package com.bigboss.millkbot.interceptor

import com.bigboss.millkbot.model.ScheduledTask
import com.bigboss.millkbot.model.ScheduledTaskDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTaskDraftInterceptor : DraftInterceptor<ScheduledTask, ScheduledTaskDraft> {

    override fun beforeSave(draft: ScheduledTaskDraft, original: ScheduledTask?) {
        val now = LocalDateTime.now()

        if (!isLoaded(draft, ScheduledTask::updatedAt)) {
            draft.updatedAt = now
        }

        if (original == null) {
            if (!isLoaded(draft, ScheduledTask::createdAt)) {
                draft.createdAt = now
            }
            if (!isLoaded(draft, ScheduledTask::createdBy)) {
                draft.createdBy = "user"
            }
            if (!isLoaded(draft, ScheduledTask::enabled)) {
                draft.enabled = true
            }
        }
    }
}