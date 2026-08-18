package de.shcreative.taskube.task.api

import de.shcreative.taskube.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@Transactional
class TaskControllerIT : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `PUT create-task returns 201 with created task`() {
        mockMvc.put("/task/create-task") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Test Task", "description": "A description", "jiraId": "PROJ-1"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Test Task") }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    fun `PUT create-task with null optional fields returns 201`() {
        mockMvc.put("/task/create-task") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Minimal Task", "description": null, "jiraId": null}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Minimal Task") }
        }
    }

    @Test
    fun `GET tasks returns list of tasks`() {
        mockMvc.put("/task/create-task") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Listed Task", "description": null, "jiraId": null}"""
        }

        mockMvc.get("/task/tasks").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$[0].title") { value("Listed Task") }
        }
    }

    @Test
    fun `GET tasks returns empty list when no tasks exist`() {
        mockMvc.get("/task/tasks").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
        }
    }
}
