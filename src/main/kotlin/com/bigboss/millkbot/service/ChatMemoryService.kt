package com.bigboss.millkbot.service

import com.bigboss.millkbot.model.AgentChatMemory
import com.bigboss.millkbot.model.conversationId
import com.bigboss.millkbot.model.createdAt
import com.bigboss.millkbot.model.id
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.messages.*
import org.springframework.stereotype.Component

@Component
class ChatMemoryService(
    private val sqlClient: KSqlClient,
) : ChatMemoryRepository {

    override fun findConversationIds(): List<String> {
        return sqlClient.createQuery(AgentChatMemory::class) {
            select(table)
        }.execute()
            .map { it.conversationId }
            .distinct()
    }

    override fun findByConversationId(conversationId: String): List<Message> {
        return sqlClient.createQuery(AgentChatMemory::class) {
            where(table.conversationId eq conversationId)
            orderBy(table.createdAt.asc(), table.id.asc())
            select(table)
        }.execute().map { row ->
            toMessage(row.role, row.content)
        }
    }

    override fun saveAll(conversationId: String, messages: List<Message>) {
        sqlClient.createDelete(AgentChatMemory::class) {
            where(table.conversationId eq conversationId)
        }.execute()

        messages.forEach { message ->
            val messageType = message.messageType
            if (messageType != MessageType.USER && messageType != MessageType.ASSISTANT && messageType != MessageType.SYSTEM) {
                return@forEach
            }

            sqlClient.save(AgentChatMemory {
                this.conversationId = conversationId
                role = messageType.value
                content = message.text
            }, SaveMode.INSERT_ONLY)
        }
    }

    override fun deleteByConversationId(conversationId: String) {
        sqlClient.createDelete(AgentChatMemory::class) {
            where(table.conversationId eq conversationId)
        }.execute()
    }

    private fun toMessage(role: String, content: String?): Message {
        val messageContent = content ?: ""
        return when (role) {
            MessageType.USER.value -> UserMessage(messageContent)
            MessageType.SYSTEM.value -> SystemMessage(messageContent)
            else -> AssistantMessage(messageContent)
        }
    }
}
