package de.shcreative.taskube.tracking.domain

import de.shcreative.taskube.task.persistence.TaskEntity
import de.shcreative.taskube.task.persistence.TaskRepository
import de.shcreative.taskube.tracking.persistence.TrackingEntity
import de.shcreative.taskube.tracking.persistence.TrackingRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertTrue
import kotlin.time.Duration

@ExtendWith(MockitoExtension::class)
class TrackingServiceTest {

    @Mock
    lateinit var trackingRepository: TrackingRepository

    @Mock
    lateinit var taskRepository: TaskRepository

    @InjectMocks
    lateinit var trackingService: TrackingService

    @Test
    fun `track saves new event when no open tracking exists`() {
        val newEvent = TrackingEntity(taskId = UUID.randomUUID(), startTime = Instant.now())
        `when`(trackingRepository.findFirstByEndTimeIsNull()).thenReturn(null)

        trackingService.track(newEvent)

        verify(trackingRepository).save(newEvent)
        verify(taskRepository, never()).findById(any(UUID::class.java))
    }

    @Test
    fun `track closes last open event and saves both`() {
        val lastTracking = TrackingEntity(id = UUID.randomUUID(), UUID.randomUUID(), startTime = Instant.now().minusSeconds(60))
        val newEvent = TrackingEntity(taskId = UUID.randomUUID(), startTime = Instant.now())
        `when`(trackingRepository.findFirstByEndTimeIsNull()).thenReturn(lastTracking)
        `when`(taskRepository.findById(any(UUID::class.java))).thenReturn(Optional.empty())

        trackingService.track(newEvent)

        verify(trackingRepository).save(lastTracking)
        verify(trackingRepository).save(newEvent)
    }

    @Test
    fun `track updates task spent time when associated task is found`() {
        val taskId = UUID.randomUUID()
        val task = TaskEntity(id = taskId, title = "Test", description = null, jiraId = null)
        val lastTracking = TrackingEntity(
            id = UUID.randomUUID(),
            taskId = taskId,
            startTime = Instant.now().minusSeconds(30),
        )
        val newEvent = TrackingEntity(taskId = UUID.randomUUID(), startTime = Instant.now())
        `when`(trackingRepository.findFirstByEndTimeIsNull()).thenReturn(lastTracking)
        `when`(taskRepository.findById(taskId)).thenReturn(Optional.of(task))

        trackingService.track(newEvent)

        assertTrue(task.spentTime > Duration.ZERO, "Expected spentTime to be updated")
    }
}
