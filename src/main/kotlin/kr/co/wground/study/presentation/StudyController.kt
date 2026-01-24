package kr.co.wground.study.presentation

import jakarta.validation.Valid
import kr.co.wground.global.jwt.UserPrincipal
import kr.co.wground.study.application.StudyService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.presentation.request.study.StudyCreateRequest
import kr.co.wground.study.presentation.request.study.StudyUpdateRequest
import kr.co.wground.study.presentation.response.study.StudyDetailResponse
import kr.co.wground.study.presentation.response.study.StudyIdResponse
import kr.co.wground.user.domain.constant.UserRole
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/studies")
class StudyController(
    private val studyService: StudyService
) {

    @PostMapping
    fun createStudy(
        userId: CurrentUserId,
        @RequestBody @Valid request: StudyCreateRequest
    ): ResponseEntity<StudyIdResponse> {
        val studyId = studyService.createStudy(request.toCommand(userId.value))
        return ResponseEntity.status(HttpStatus.CREATED).body(StudyIdResponse(studyId))
    }

    @GetMapping("/{studyId}")
    fun getStudy(
        userId: CurrentUserId,
        @PathVariable studyId: Long
    ): ResponseEntity<StudyDetailResponse> {
        val response = studyService.getStudy(studyId, userId.value)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{studyId}")
    fun updateStudy(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyUpdateRequest
    ): ResponseEntity<Unit> {
        studyService.updateStudy(request.toCommand(studyId, userId.value))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{studyId}")
    fun deleteStudy(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable studyId: Long
    ): ResponseEntity<Unit> {
        val isAdmin = if (userPrincipal.role == UserRole.ADMIN.name) {
            true
        } else {
            false
        }

        studyService.deleteStudy(studyId, userPrincipal.userId, isAdmin)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{studyId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    fun approveStudy(
        @PathVariable studyId: Long
    ): ResponseEntity<Unit> {
        studyService.approveStudy(studyId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{studyId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    fun rejectStudy(
        @PathVariable studyId: Long
    ): ResponseEntity<Unit> {
        studyService.rejectStudy(studyId)
        return ResponseEntity.noContent().build()
    }
}