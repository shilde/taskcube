package de.shcreative.taskube.tracking.persistence

import kotlin.time.Instant

class FaceChangeEntity(
    val faceId: Int,
    val startTime: Instant
)