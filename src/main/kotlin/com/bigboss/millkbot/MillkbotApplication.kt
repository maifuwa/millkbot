package com.bigboss.millkbot

import com.bigboss.millkbot.service.MilkyService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MillkbotApplication(
    private val milkyService: MilkyService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        milkyService.startListening()
    }

}

fun main(args: Array<String>) {
    runApplication<MillkbotApplication>(*args)
}
