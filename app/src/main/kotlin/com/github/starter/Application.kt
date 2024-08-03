package com.github.starter;

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration

@SpringBootApplication(exclude = arrayOf(R2dbcAutoConfiguration::class))
open class Application

fun main() {
    SpringApplication.run(Application::class.java)
}

