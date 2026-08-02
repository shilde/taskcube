package de.shcreative.taskube

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskubeApplication

fun main(args: Array<String>) {
	runApplication<TaskubeApplication>(*args)
}
