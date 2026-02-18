package kr.co.wground.study.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.presentation.request.study_report.StudyReportCancelRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportRejectRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportUpsertRequest
import kr.co.wground.study.presentation.response.study_report.StudyReportApprovalHistoryResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportIdResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportSubmissionStatusResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Study Report", description = "스터디 결과 보고/결재 API")
interface StudyReportApi {

    @Operation(summary = "스터디 결과 보고 상신", description = "스터디장이 결과 보고를 작성 또는 수정하고 상신합니다.")
    fun upsertAndSubmitReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportUpsertRequest,
    ): ResponseEntity<StudyReportIdResponse>

    @Operation(summary = "내 결과 보고 상신 상태 조회", description = "스터디장의 결과 보고 상신 상태를 조회합니다.")
    fun getMyReportSubmissionStatus(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<StudyReportSubmissionStatusResponse>

    @Operation(summary = "스터디 결과 보고 승인", description = "관리자가 상신된 결과 보고를 승인합니다.")
    fun approveReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 반려", description = "관리자가 상신된 결과 보고를 반려합니다.")
    fun rejectReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportRejectRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 결재 취소", description = "관리자가 승인/반려를 취소해 SUBMITTED 상태로 되돌립니다.")
    fun cancelApprovalOrRejection(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportCancelRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 결재 이력 조회", description = "관리자가 결재 액션 이력을 최신순으로 조회합니다.")
    fun getReportApprovalHistories(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<List<StudyReportApprovalHistoryResponse>>
}
