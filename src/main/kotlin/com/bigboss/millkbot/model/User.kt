package com.bigboss.millkbot.model

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
interface User {

    @Id
    val id: Long

    val name: String

    val relation: String

    val customPrompt: String?

    val createdAt: LocalDateTime

    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "user")
    val scheduledTask: List<ScheduledTask>
}
