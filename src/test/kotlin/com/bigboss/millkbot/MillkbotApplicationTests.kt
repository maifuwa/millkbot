package com.bigboss.millkbot

import com.bigboss.millkbot.model.User
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MillkbotApplicationTests {

    @Autowired
    lateinit var sqlClient: KSqlClient

    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun contextLoads() {
        val users = sqlClient
            .createQuery(User::class) {
                select(table)
            }.execute()
        logger.debug("Loaded {} users in contextLoads()", users.size)
    }

}
