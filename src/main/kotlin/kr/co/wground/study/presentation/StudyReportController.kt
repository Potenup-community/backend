package kr.co.wground.study.presentation

import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.application.StudyReportAdminService
import kr.co.wground.study.application.StudyReportService
import kr.co.wground.study.presentation.request.study_report.StudyReportCancelRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportRejectRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportUpsertRequest
import kr.co.wground.study.presentation.response.study_report.StudyReportApprovalHistoryResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportIdResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportSubmissionStatusResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class StudyReportController(
    private val studyReportService: StudyReportService,
    private val studyReportAdminService: StudyReportAdminService,
) : StudyReportApi {

    @PostMapping("/studies/{studyId}/report")
    override fun upsertAndSubmitReport(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportUpsertRequest,
    ): ResponseEntity<StudyReportIdResponse> {
        val reportId = studyReportService.upsertAndSubmit(request.toCommand(studyId, userId.value))
        return ResponseEntity.status(HttpStatus.CREATED).body(StudyReportIdResponse(reportId))
    }

    @GetMapping("/users/me/studies/{studyId}/report/submission-status")
    override fun getMyReportSubmissionStatus(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<StudyReportSubmissionStatusResponse> {
        val queryResult = studyReportService.getMySubmissionStatus(studyId, userId.value)
        return ResponseEntity.ok(StudyReportSubmissionStatusResponse.from(queryResult))
    }

    @PatchMapping("/studies/{studyId}/report/approve")
    @PreAuthorize("hasRole('ADMIN')")
    override fun approveReport(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<Unit> {
        studyReportAdminService.approve(studyId, userId.value)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/studies/{studyId}/report/reject")
    @PreAuthorize("hasRole('ADMIN')")
    override fun rejectReport(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportRejectRequest,
    ): ResponseEntity<Unit> {
        studyReportAdminService.reject(studyId, userId.value, request.reason)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/studies/{studyId}/report/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    override fun cancelApprovalOrRejection(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportCancelRequest,
    ): ResponseEntity<Unit> {
        studyReportAdminService.cancel(studyId, userId.value, request.reason)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/studies/{studyId}/report/approval-histories")
    @PreAuthorize("hasRole('ADMIN')")
    override fun getReportApprovalHistories(
        userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<List<StudyReportApprovalHistoryResponse>> {
        val responses = studyReportAdminService.getApprovalHistories(studyId)
            .map { StudyReportApprovalHistoryResponse.from(it) }
        return ResponseEntity.ok(responses)
    }
}
