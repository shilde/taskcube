package de.shcreative.taskube.tracking.persistence

import de.shcreative.taskube.tracking.api.dto.FaceChangeRequest

fun FaceChangeRequest.toDomain(faceId: Int) = FaceChangeEntity(
    startTime = startTime,
    faceId = faceId,
)