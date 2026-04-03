package com.inspectpro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InspectionProfileServiceApplication

fun main(args: Array<String>) {
	runApplication<InspectionProfileServiceApplication>(*args)
}
