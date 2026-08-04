package de.shcreative.taskube.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

	@Bean
	fun taskubeOpenApi(): OpenAPI = OpenAPI()
		.info(
			Info()
				.title("TaskCube Backend API")
				.description(
					"Persistence and API for the TaskCube time-tracking cube. Backed by an in-memory " +
						"store for now - see backend/CLAUDE.md for the open database/auth decisions.",
				)
				.version("0.0.1"),
		)
}
