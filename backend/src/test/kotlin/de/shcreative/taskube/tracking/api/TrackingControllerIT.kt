package de.shcreative.taskube.tracking.api

import tools.jackson.databind.ObjectMapper
import de.shcreative.taskube.AbstractIntegrationTest
import de.shcreative.taskube.tracking.api.dto.FaceChangeRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
class TrackingControllerIT : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `PUT face-change with valid face id returns 204`() {
        val body = objectMapper.writeValueAsString(FaceChangeRequest(startTime = Instant.now()))

        mockMvc.put("/face-change/1") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `PUT face-change closes previous open event when called twice`() {
        val body = objectMapper.writeValueAsString(FaceChangeRequest(startTime = Instant.now()))

        mockMvc.put("/face-change/1") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
        mockMvc.put("/face-change/2") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `PUT face-change with face id below minimum returns 400`() {
        val body = objectMapper.writeValueAsString(FaceChangeRequest(startTime = Instant.now()))

        mockMvc.put("/face-change/0") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT face-change with face id above maximum returns 400`() {
        val body = objectMapper.writeValueAsString(FaceChangeRequest(startTime = Instant.now()))

        mockMvc.put("/face-change/7") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT face-change without body returns 400`() {
        mockMvc.put("/face-change/1").andExpect {
            status { isBadRequest() }
        }
    }
}
