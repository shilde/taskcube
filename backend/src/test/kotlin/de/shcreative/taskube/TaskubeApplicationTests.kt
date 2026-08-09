package de.shcreative.taskube

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class TaskubeApplicationTests {

	companion object {
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:17-alpine")
	}

	@Test
	fun contextLoads() {
	}

}
