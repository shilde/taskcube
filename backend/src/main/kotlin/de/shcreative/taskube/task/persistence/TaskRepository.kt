package de.shcreative.taskube.task.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository : JpaRepository<TaskEntity, UUID>
