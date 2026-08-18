package de.shcreative.taskube.tracking.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface TrackingRepository : JpaRepository<TrackingEntity, UUID> {

    fun findFirstByEndTimeIsNull(): TrackingEntity?

    @Modifying
    @Query("UPDATE TrackingEntity t SET t.endTime = :endTime WHERE t.id = :id")
    fun closeSession(id: UUID, endTime: Instant)
}
