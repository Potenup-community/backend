package kr.co.wground.study_schedule.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study_schedule.application.StudyScheduleService
import kr.co.wground.study_schedule.presentation.request.ScheduleCreateRequest
import kr.co.wground.study_schedule.presentation.request.ScheduleUpdateRequest
import kr.co.wground.study_schedule.presentation.response.ScheduleCreateResponse
import kr.co.wground.study_schedule.presentation.response.ScheduleListResponse
import kr.co.wground.study_schedule.presentation.response.ScheduleQueryResponse
import kr.co.wground.study_schedule.presentation.response.ScheduleUpdateResponse
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

    // To Do: 관리자 트랙 조회 api 필요(트랙으로 필터링 가능해야 함)

    @GetMapping
    override fun getSchedules(userId: CurrentUserId): ResponseEntity<ScheduleListResponse>{
        val result = studyScheduleService.getSchedulesByUserId(userId.value)
        return ResponseEntity.ok().body(ScheduleListResponse(result))
    }

    @GetMapping("/me")
    override fun getCurrentSchedule(userId: CurrentUserId): ResponseEntity<ScheduleQueryResponse>{
        val result = studyScheduleService.getCurrentScheduleByUserId(userId.value)
        return ResponseEntity.ok().body(result)
    }
}