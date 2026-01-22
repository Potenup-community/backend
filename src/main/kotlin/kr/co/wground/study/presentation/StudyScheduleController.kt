package kr.co.wground.study.presentation

import kr.co.wground.study.application.StudyScheduleService
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.presentation.request.schedule.ScheduleCreateRequest
import kr.co.wground.study.presentation.request.schedule.ScheduleUpdateRequest
import kr.co.wground.study.presentation.response.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.ScheduleUpdateResponse
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
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/studies/schedules")
class StudyScheduleController(
    private val studyScheduleService: StudyScheduleService
) {
    @PostMapping
    fun createSchedule(@RequestBody request: ScheduleCreateRequest): ResponseEntity<ScheduleCreateResponse> {
        val response = studyScheduleService.createSchedule(request.toCommand())
        return ResponseEntity.ok().body(response)
    }

    @PatchMapping("/{id}")
    fun updateSchedule(@RequestBody request: ScheduleUpdateRequest, @PathVariable id: Long): ResponseEntity<ScheduleUpdateResponse> {
        val response = studyScheduleService.updateSchedule(request.toCommand(id))
        return ResponseEntity.ok().body(response)
    }

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: Long): ResponseEntity<Unit> {
        studyScheduleService.deleteSchedule(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getAllSchedules(){
        val response = studyScheduleService.getAllSchedules()
    }
}