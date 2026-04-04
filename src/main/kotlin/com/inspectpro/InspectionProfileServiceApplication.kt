package com.inspectpro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class InspectionProfileServiceApplication

fun main(args: Array<String>) {
	runApplication<InspectionProfileServiceApplication>(*args)
}
