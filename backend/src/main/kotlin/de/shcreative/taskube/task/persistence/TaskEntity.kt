package de.shcreative.taskube.task.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "task")
class TaskEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val title: String,
    val description: String?,
    @field:Column(name = "jiraid")
    val jiraId: String?,
)