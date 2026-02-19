package kr.co.wground.study.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.application.dto.StudyReportSearchCondition
import kr.co.wground.study.docs.StudySwaggerErrorExample
import kr.co.wground.study.docs.StudySwaggerResponseExample
import kr.co.wground.study.presentation.request.study_report.StudyReportCancelRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportRejectRequest
import kr.co.wground.study.presentation.request.study_report.StudyReportUpsertRequest
import kr.co.wground.study.presentation.response.CustomSliceResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportApprovalHistoryResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportDetailResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportIdResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportSummaryResponse
import kr.co.wground.study.presentation.response.study_report.StudyReportSubmissionStatusResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Study Report", description = "스터디 결과 보고/결재 API")
interface StudyReportApi {

    @Operation(summary = "스터디 결과 보고 상신", description = "스터디장이 결과 보고를 작성 또는 수정하고 상신합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "상신 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyReportIdResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_REPORT_ID_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_WEEKLY_ACTIVITIES_INVALID",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_WEEKLY_ACTIVITIES_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_REPORT_TEAM_RETROSPECTIVE_INVALID",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_TEAM_RETROSPECTIVE_INVALID
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_STUDY_LEADER",
                            value = StudySwaggerErrorExample.Study.NOT_STUDY_LEADER
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_NOT_FOUND",
                            value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "409", description = "상신 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_UPDATE_NOT_ALLOWED_FOR_STUDY_STATUS",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_UPDATE_NOT_ALLOWED_FOR_STUDY_STATUS
                        ),
                        ExampleObject(
                            name = "STUDY_REPORT_CANNOT_UPDATE_AFTER_APPROVED",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_CANNOT_UPDATE_AFTER_APPROVED
                        ),
                    ]
                )]
            )
        ]
    )
    fun upsertAndSubmitReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportUpsertRequest,
    ): ResponseEntity<StudyReportIdResponse>

    @Operation(summary = "내 결과 보고 상신 상태 조회", description = "스터디장의 결과 보고 상신 상태를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyReportSubmissionStatusResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_REPORT_SUBMISSION_STATUS_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_STUDY_LEADER",
                            value = StudySwaggerErrorExample.Study.NOT_STUDY_LEADER
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_NOT_FOUND",
                            value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun getMyReportSubmissionStatus(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<StudyReportSubmissionStatusResponse>

    @Operation(summary = "스터디 결과 보고 상세 조회", description = "관리자 또는 해당 스터디장이 결과 보고 상세를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyReportDetailResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_REPORT_DETAIL_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_STUDY_LEADER",
                            value = StudySwaggerErrorExample.Study.NOT_STUDY_LEADER
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun getStudyReportDetail(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<StudyReportDetailResponse>

    @Operation(summary = "스터디 결과 보고 목록 조회", description = "관리자가 결과 보고 목록을 조건으로 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CustomSliceResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_REPORT_LIST_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_STUDY_LEADER",
                            value = StudySwaggerErrorExample.Study.NOT_STUDY_LEADER
                        ),
                    ]
                )]
            ),
        ]
    )
    fun searchStudyReports(
        @ParameterObject @ModelAttribute condition: StudyReportSearchCondition,
        @ParameterObject @PageableDefault(size = 20, sort = ["submittedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<CustomSliceResponse<StudyReportSummaryResponse>>

    @Operation(summary = "스터디 결과 보고 승인", description = "관리자가 상신된 결과 보고를 승인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "승인 성공"),
            ApiResponse(
                responseCode = "400", description = "처리 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_STATUS_TRANSITION_INVALID",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_STATUS_TRANSITION_INVALID
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun approveReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 반려", description = "관리자가 상신된 결과 보고를 반려합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "반려 성공"),
            ApiResponse(
                responseCode = "400", description = "처리 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_STATUS_TRANSITION_INVALID",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_STATUS_TRANSITION_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_REPORT_REJECT_REASON_REQUIRED",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_REJECT_REASON_REQUIRED
                        ),
                        ExampleObject(
                            name = "STUDY_REPORT_REASON_TOO_LONG",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_REASON_TOO_LONG
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun rejectReport(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportRejectRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 결재 취소", description = "관리자가 승인/반려를 취소해 SUBMITTED 상태로 되돌립니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 성공"),
            ApiResponse(
                responseCode = "400", description = "처리 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_STATUS_TRANSITION_INVALID",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_STATUS_TRANSITION_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_REPORT_REASON_TOO_LONG",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_REASON_TOO_LONG
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun cancelApprovalOrRejection(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyReportCancelRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 결과 보고 결재 이력 조회", description = "관리자가 결재 액션 이력을 최신순으로 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyReportApprovalHistoryResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_REPORT_APPROVAL_HISTORY_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_REPORT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Report.STUDY_REPORT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun getReportApprovalHistories(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<List<StudyReportApprovalHistoryResponse>>
}
