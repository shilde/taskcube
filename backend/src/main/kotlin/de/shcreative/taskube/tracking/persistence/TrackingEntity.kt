package de.shcreative.taskube.tracking.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tracking")
class TrackingEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @field:Column(name = "task_id")
    val taskId: UUID?,
    @field:Column(name = "starttime")
    val startTime: Instant,
    @field:Column(name = "endtime")
    var endTime: Instant? = null,
)