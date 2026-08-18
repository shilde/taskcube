package de.shcreative.taskube.cube.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface FaceConfigRepository : JpaRepository<FaceConfigEntity, Int> {

    @Modifying
    @Query("UPDATE FaceConfigEntity f SET f.taskId = :taskId WHERE f.faceId = :faceId")
    fun assignTask(faceId: Int, taskId: UUID)

    @Query("SELECT f.taskId FROM FaceConfigEntity f WHERE f.faceId = :faceId")
    fun getTask(faceId: Int): UUID?
}
