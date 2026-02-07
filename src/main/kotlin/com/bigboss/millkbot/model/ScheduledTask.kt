package com.bigboss.millkbot.model

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "scheduled_tasks")
interface ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val cronExpr: String

    val content: String

    val createdBy: String

    val enabled: Boolean

    val createdAt: LocalDateTime

    val updatedAt: LocalDateTime

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User
}