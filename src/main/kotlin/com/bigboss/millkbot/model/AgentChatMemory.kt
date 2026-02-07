package com.bigboss.millkbot.model

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "agent_chat_memories")
interface AgentChatMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val conversationId: String

    val role: String

    val content: String?

    val createdAt: LocalDateTime
}
