package kr.co.wground.study.presentation

import kr.co.wground.study.application.StudyScheduleService
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.presentation.request.schedule.ScheduleCreateRequest
import kr.co.wground.study.presentation.request.schedule.ScheduleUpdateRequest
import kr.co.wground.study.presentation.response.ScheduleCreateResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/studies/schedule")
class StudyScheduleController(
    private val studyScheduleService: StudyScheduleService
) {
    @PostMapping
    fun createSchedule(@RequestBody request: ScheduleCreateRequest): ResponseEntity<ScheduleCreateResponse> {
        val response = studyScheduleService.createSchedule(request.toCommand())
        return ResponseEntity.ok().body(response)
    }

    @PatchMapping
    fun updateSchedule(@RequestBody request: ScheduleUpdateRequest): ResponseEntity<StudySchedule> {
        val response = studyScheduleService.updateSchedule(request.toCommand())
    }
}