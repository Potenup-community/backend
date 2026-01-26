package kr.co.wground.study.presentation

import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.application.StudyRecruitmentService
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.presentation.request.recruitment.StudyRecruitRequest
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class StudyRecruitmentController(
    private val studyRecruitmentService: StudyRecruitmentService
): StudyRecruitmentApi {
    @PostMapping("/studies/{studyId}/recruitments")
    override fun applyStudy(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyRecruitRequest
    ): ResponseEntity<Unit> {
        studyRecruitmentService.requestRecruit(userId.value, studyId, request.appeal)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/studies/{studyId}/recruitments/{recruitmentId}")
    override fun cancelApplication(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @PathVariable recruitmentId: Long
    ): ResponseEntity<Unit> {
        studyRecruitmentService.cancelRecruit(userId.value, recruitmentId)
        return ResponseEntity.noContent().build()
    }

    // 신청 승인 (스터디장)
    @PatchMapping("/studies/{studyId}/recruitments/{recruitmentId}/approve")
    override fun approveApplication(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @PathVariable recruitmentId: Long
    ): ResponseEntity<Unit> {
        studyRecruitmentService.determineRecruit(userId.value, recruitmentId, RecruitStatus.APPROVED)
        return ResponseEntity.noContent().build()
    }

    // 신청 반려 (스터디장)
    @PatchMapping("/studies/{studyId}/recruitments/{recruitmentId}/reject")
    override fun rejectApplication(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @PathVariable recruitmentId: Long
    ): ResponseEntity<Unit> {
        studyRecruitmentService.determineRecruit(userId.value, recruitmentId, RecruitStatus.REJECTED)
        return ResponseEntity.noContent().build()
    }

    // 내 신청 목록 조회
    @GetMapping("/users/me/recruitments")
    override fun getMyRecruitments(
        userId: CurrentUserId
    ): ResponseEntity<List<StudyRecruitmentResponse>> {
        val responses = studyRecruitmentService.getMyRecruitments(userId.value)
        return ResponseEntity.ok(responses)
    }

    // 스터디 신청자 목록 조회 (스터디장)
    @GetMapping("/users/me/studies/{studyId}/recruitments")
    override fun getStudyRecruitments(
        userId: CurrentUserId,
        @PathVariable studyId: Long
    ): ResponseEntity<List<StudyRecruitmentResponse>> {
        val responses = studyRecruitmentService.getStudyRecruitments(userId.value, studyId)
        return ResponseEntity.ok(responses)
    }
}
