package de.shcreative.taskube.task.api

import de.shcreative.taskube.cube.api.dto.AssignTaskRequest
import de.shcreative.taskube.task.api.dto.CreateTaskRequest
import de.shcreative.taskube.task.api.dto.TaskDto
import de.shcreative.taskube.task.domain.TaskService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/task")
class TaskController(
    private val taskService: TaskService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/create-task")
    fun createTask(@RequestBody request: CreateTaskRequest) = taskService.createTask(request.title, request.description, request.jiraId)

    @GetMapping("/tasks")
    fun getTasks(): List<TaskDto> =
        taskService.getTasks().map { TaskDto(id = it.id!!, title = it.title, description = it.description, jiraId = it.jiraId) }
}