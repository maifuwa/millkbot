package com.bigboss.millkbot.service

import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.model.customPrompt
import com.bigboss.millkbot.model.id
import com.bigboss.millkbot.model.relation
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val sqlClient: KSqlClient
) {

    @Transactional
    fun getUser(id: Long, name: String): User {
        var user = sqlClient.createQuery(User::class) {
            where(table.id eq id)
            select(table)
        }.execute().singleOrNull()

        if (user == null) {
            user = sqlClient.save(User {
                this.id = id
                this.name = name
            }, SaveMode.INSERT_ONLY).modifiedEntity
        }

        return user
    }

    @Transactional
    fun createMaster(id: Long): Boolean {
        if (hasMaster()) return false
        return sqlClient.createUpdate(User::class) {
            set(table.relation, "Master")
            where(table.id eq id)
        }.execute() > 0
    }

    fun hasMaster(): Boolean {
        return sqlClient.createQuery(User::class) {
            where(table.relation eq "Master")
            select(count(table.id))
        }.execute().single() > 0
    }

    @Transactional
    fun updateUserRelation(operator: User, changeId: Long, relation: String): Boolean {
        if (operator.relation != "Master") return false
        return sqlClient.createUpdate(User::class) {
            set(table.relation, relation)
            where(table.id eq changeId)
        }.execute() > 0
    }

    @Transactional
    fun createPrompt(id: Long, prompt: String): Boolean {
        return sqlClient.createUpdate(User::class) {
            set(table.customPrompt, prompt)
            where(table.id eq id)
        }.execute() > 0
    }

}
