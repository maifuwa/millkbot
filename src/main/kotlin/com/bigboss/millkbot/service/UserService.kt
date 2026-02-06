package com.bigboss.millkbot.service

import com.bigboss.millkbot.model.User
import com.bigboss.millkbot.model.id
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.createQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val sqlClient: KSqlClient
) {

    @Transactional
    fun getUser(id: Long, name: String): User {
        var user = sqlClient.createQuery {
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
}