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
    @field:Column(name = "faceid")
    val faceId: Int,
    @field:Column(name = "taskid")
    var taskId: UUID?,
)
