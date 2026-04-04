package com.inspectpro

import com.inspectpro.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties::class)
class InspectionProfileServiceApplication

fun main(args: Array<String>) {
    runApplication<InspectionProfileServiceApplication>(*args)
}
