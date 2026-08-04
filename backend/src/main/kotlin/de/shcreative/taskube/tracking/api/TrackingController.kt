package de.shcreative.taskube.tracking.api

import de.shcreative.taskube.tracking.api.dto.FaceChangeRequest
import de.shcreative.taskube.tracking.domain.TrackingService
import de.shcreative.taskube.tracking.persistence.toDomain
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/face-change")
class TrackingController(
    private val trackingService: TrackingService
) {

    @PutMapping("/{faceId}")
    fun recordFaceChange(@PathVariable faceId: Int, @RequestBody faceChange: FaceChangeRequest) =
        trackingService.track(faceChange.toDomain(faceId))
}