package de.shcreative.taskube.cube.api

import de.shcreative.taskube.cube.api.dto.AssignTaskRequest
import de.shcreative.taskube.cube.api.dto.FaceConfigDto
import de.shcreative.taskube.cube.domain.FaceConfigService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/face-config")
class FaceConfigController(private val service: FaceConfigService) {

    @PutMapping("/{faceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignTask(
        @PathVariable @Min(1) @Max(6) faceId: Int,
        @RequestBody body: AssignTaskRequest
    ) = service.assignTask(faceId, body.taskId)

    @GetMapping("/faces")
    fun getFaces(): List<FaceConfigDto> =
        service.getTasks().mapNotNull { it.taskId?.let { id -> FaceConfigDto(faceId = it.faceId, taskId = id) } }
}
