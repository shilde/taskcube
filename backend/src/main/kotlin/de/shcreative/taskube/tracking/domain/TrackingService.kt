package de.shcreative.taskube.tracking.domain

import de.shcreative.taskube.task.persistence.TaskRepository
import de.shcreative.taskube.tracking.persistence.TrackingEntity
import de.shcreative.taskube.tracking.persistence.TrackingRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull
import kotlin.time.toKotlinDuration

@Service
@Transactional
class TrackingService(
    private val trackingRepository: TrackingRepository,
    private val taskRepository: TaskRepository
) {
    fun track(newTrackingEvent: TrackingEntity) {
        trackingRepository.findFirstByEndTimeIsNull()?.also { lastTracking ->
            lastTracking.endTime = newTrackingEvent.startTime
            lastTracking.taskId?.let { taskRepository.findById(it).getOrNull() }?.also {
                it.spentTime = it.spentTime.plus(
                    java.time.Duration.between(lastTracking.startTime, newTrackingEvent.startTime).toKotlinDuration()
                )
            }
            trackingRepository.save(lastTracking)
        }

        trackingRepository.save(newTrackingEvent)
    }
}
