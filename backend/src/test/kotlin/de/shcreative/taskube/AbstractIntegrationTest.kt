package de.shcreative.taskube

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
            start()
            println("=== Testcontainers PostgreSQL ===")
            println("JDBC URL:  $jdbcUrl")
            println("Username:  $username")
            println("Password:  $password")
            println("=================================")
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
