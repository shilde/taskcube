package de.shcreative.taskube.cube.domain

import de.shcreative.taskube.cube.persistence.FaceConfigEntity
import de.shcreative.taskube.cube.persistence.FaceConfigRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FaceConfigService(private val repository: FaceConfigRepository) {

    fun assignTask(faceId: Int, taskId: UUID) = repository.assignTask(faceId, taskId)

    fun getTasks(): List<FaceConfigEntity> = repository.findAll()
}
