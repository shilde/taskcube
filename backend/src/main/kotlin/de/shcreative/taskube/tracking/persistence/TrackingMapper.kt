package de.shcreative.taskube.tracking.persistence

import de.shcreative.taskube.tracking.api.dto.FaceChangeRequest
import java.util.UUID
import kotlin.time.toJavaInstant

fun FaceChangeRequest.toDomain(taskId: UUID?) = TrackingEntity(
    taskId = taskId,
    startTime = startTime.toJavaInstant(),
)