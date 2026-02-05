package com.bigboss.millkbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MillkbotApplication

fun main(args: Array<String>) {
    runApplication<MillkbotApplication>(*args)
}
