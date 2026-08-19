package de.shcreative.taskube.task.api.dto

import java.util.UUID

data class TaskDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val jiraId: String?,
    val spentTimeMs: Long
)