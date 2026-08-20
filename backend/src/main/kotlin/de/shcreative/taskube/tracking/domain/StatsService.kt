package de.shcreative.taskube.tracking.domain

import de.shcreative.taskube.task.persistence.TaskRepository
import de.shcreative.taskube.tracking.api.dto.TaskSummaryDto
import de.shcreative.taskube.tracking.persistence.TrackingRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class StatsService(
    private val trackingRepository: TrackingRepository,
    private val taskRepository: TaskRepository,
) {
    fun summarize(from: Instant, to: Instant): List<TaskSummaryDto> {
        val now = Instant.now()
        val sessions = trackingRepository.findSessions(from, to)

        val durationByTaskId = mutableMapOf<UUID, Duration>()
        for (session in sessions) {
            val effectiveStart = maxOf(session.startTime, from)
            val effectiveEnd = minOf(session.endTime ?: now, to)
            val duration = Duration.between(effectiveStart, effectiveEnd)
            if (!duration.isNegative && !duration.isZero) {
                durationByTaskId.merge(session.taskId!!, duration, Duration::plus)
            }
        }

        val tasks = taskRepository.findAllById(durationByTaskId.keys).associateBy { it.id!! }

        return durationByTaskId
            .mapNotNull { (taskId, duration) ->
                val task = tasks[taskId] ?: return@mapNotNull null
                TaskSummaryDto(
                    taskId = taskId,
                    title = task.title,
                    totalSpentTimeMs = duration.toMillis(),
                )
            }
            .sortedByDescending { it.totalSpentTimeMs }
    }
}
