package de.shcreative.taskube.task.api.dto

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val jiraId: String?,
)
