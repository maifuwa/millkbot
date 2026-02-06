package com.bigboss.millkbot.interceptor

import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.model.UserDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserDraftInterceptor : DraftInterceptor<User, UserDraft> {

    override fun beforeSave(draft: UserDraft, original: User?) {
        val now = LocalDateTime.now()

        if (!isLoaded(draft, User::updatedAt)) {
            draft.updatedAt = now
        }

        if (original == null) {
            if (!isLoaded(draft, User::createdAt)) {
                draft.createdAt = now
            }
            if (!isLoaded(draft, User::relation)) {
                draft.relation = "Guest"
            }
        }
    }
}
