package de.shcreative.taskube.cube.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "face_config")
class FaceConfigEntity(
    @field:Id
    @field:Column(name = "face_id")
    val faceId: Int,
    @field:Column(name = "task_id")
    var taskId: UUID?,
)
