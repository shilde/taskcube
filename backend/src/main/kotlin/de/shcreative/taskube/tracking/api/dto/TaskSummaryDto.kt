package de.shcreative.taskube.tracking.api.dto

import java.util.UUID

data class TaskSummaryDto(
    val taskId: UUID,
    val title: String,
    val totalSpentTimeMs: Long,
)
