package de.shcreative.taskube.task.domain

import de.shcreative.taskube.task.persistence.TaskEntity
import de.shcreative.taskube.task.persistence.TaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    fun createTask(title: String, description: String?, jiraId: String?): TaskEntity =
        taskRepository.save(TaskEntity(title = title, description = description, jiraId = jiraId))

    fun getTasks(): List<TaskEntity> = taskRepository.findAll()
}