package com.inspectpro

import com.inspectpro.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties::class)
@EnableScheduling
class InspectionProfileServiceApplication

fun main(args: Array<String>) {
    runApplication<InspectionProfileServiceApplication>(*args)
}
