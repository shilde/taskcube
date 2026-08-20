package de.shcreative.taskube.tracking.api

import de.shcreative.taskube.tracking.api.dto.TaskSummaryDto
import de.shcreative.taskube.tracking.domain.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/stats")
class StatsController(private val statsService: StatsService) {

    @GetMapping
    fun getSummary(
        @RequestParam from: Instant,
        @RequestParam to: Instant,
    ): List<TaskSummaryDto> = statsService.summarize(from, to)
}
