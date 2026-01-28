package kr.co.wground.study.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.application.StudyScheduleService
import kr.co.wground.study.presentation.request.schedule.ScheduleCreateRequest
import kr.co.wground.study.presentation.request.schedule.ScheduleUpdateRequest
import kr.co.wground.study.presentation.response.schedule.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleListResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleQueryResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleUpdateResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/studies/schedules")
class StudyScheduleController(
    private val studyScheduleService: StudyScheduleService
): StudyScheduleApi {
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    override fun createSchedule(@RequestBody request: ScheduleCreateRequest): ResponseEntity<ScheduleCreateResponse> {
        val response = studyScheduleService.createSchedule(request.toCommand())
        return ResponseEntity.ok().body(response)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    override fun updateSchedule(@RequestBody request: ScheduleUpdateRequest, @PathVariable id: Long): ResponseEntity<ScheduleUpdateResponse> {
        val response = studyScheduleService.updateSchedule(request.toCommand(id))
        return ResponseEntity.ok().body(response)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    override fun deleteSchedule(@PathVariable id: Long): ResponseEntity<Unit> {
        studyScheduleService.deleteSchedule(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    override fun getSchedules(userId: CurrentUserId): ResponseEntity<ScheduleListResponse>{
        val result = studyScheduleService.getSchedules(userId.value)
        return ResponseEntity.ok().body(ScheduleListResponse(result))
    }

    @GetMapping("/me")
    override fun getCurrentSchedule(userId: CurrentUserId): ResponseEntity<ScheduleQueryResponse>{
        val result = studyScheduleService.getCurrentScheduleByUserId(userId.value)
        return ResponseEntity.ok().body(result)
    }
}