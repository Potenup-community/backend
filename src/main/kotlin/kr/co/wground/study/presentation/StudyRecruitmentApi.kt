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
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.study.docs.StudySwaggerErrorExample
import kr.co.wground.study.docs.StudySwaggerResponseExample
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentListResponse
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Study Recruitment", description = "스터디 모집/신청 API")
interface StudyRecruitmentApi {

    @Operation(summary = "스터디 참여", description = "스터디에 참여 신청을 합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "신청 성공"),
            ApiResponse(
                responseCode = "400", description = "신청 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TRACK_MISMATCH",
                            value = StudySwaggerErrorExample.Recruitment.TRACK_MISMATCH
                        ),
                        ExampleObject(
                            name = "STUDY_NOT_RECRUITING",
                            value = StudySwaggerErrorExample.Recruitment.STUDY_NOT_RECRUITING
                        ),
                        ExampleObject(
                            name = "ALREADY_APPLIED",
                            value = StudySwaggerErrorExample.Recruitment.ALREADY_APPLIED
                        ),
                        ExampleObject(
                            name = "STUDY_MONTH_IS_NOT_CURRENT_MONTH",
                            value = StudySwaggerErrorExample.Recruitment.STUDY_MONTH_IS_NOT_CURRENT_MONTH
                        ),
                        ExampleObject(
                            name = "MAX_STUDY_EXCEEDED",
                            value = StudySwaggerErrorExample.Study.MAX_STUDY_EXCEEDED
                        ),
                        ExampleObject(
                            name = "STUDY_CAPACITY_FULL",
                            value = StudySwaggerErrorExample.Study.STUDY_CAPACITY_FULL
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
                        ExampleObject(name = "STUDY_NOT_FOUND", value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND),
                        ExampleObject(
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun applyStudy(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 강제 참여", description = "관리자가 특정 사용자를 특정 스터디에 강제로 참여시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "신청 성공"),
            ApiResponse(
                responseCode = "400", description = "신청 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TRACK_MISMATCH",
                            value = StudySwaggerErrorExample.Recruitment.TRACK_MISMATCH
                        ),
                        ExampleObject(
                            name = "STUDY_NOT_RECRUITING",
                            value = StudySwaggerErrorExample.Recruitment.CANNOT_FORCE_JOIN_AFTER_APPROVAL
                        ),
                        ExampleObject(
                            name = "ALREADY_APPLIED",
                            value = StudySwaggerErrorExample.Recruitment.ALREADY_APPLIED
                        ),
                        ExampleObject(
                            name = "STUDY_MONTH_IS_NOT_CURRENT_MONTH",
                            value = StudySwaggerErrorExample.Recruitment.STUDY_MONTH_IS_NOT_CURRENT_MONTH
                        ),
                        ExampleObject(
                            name = "MAX_STUDY_EXCEEDED",
                            value = StudySwaggerErrorExample.Study.MAX_STUDY_EXCEEDED
                        ),
                        ExampleObject(
                            name = "STUDY_CAPACITY_FULL",
                            value = StudySwaggerErrorExample.Study.STUDY_CAPACITY_FULL
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
                        ExampleObject(name = "STUDY_NOT_FOUND", value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND),
                        ExampleObject(
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun forceJoinStudy(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 신청 취소", description = "스터디 신청을 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 성공"),
            ApiResponse(
                responseCode = "400", description = "취소 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "LEADER_CANNOT_LEAVE",
                            value = StudySwaggerErrorExample.Recruitment.LEADER_CANNOT_LEAVE
                        ),
                        ExampleObject(
                            name = "RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING",
                            value = StudySwaggerErrorExample.Recruitment.RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING
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
                            name = "NOT_RECRUITMENT_OWNER",
                            value = StudySwaggerErrorExample.Recruitment.NOT_RECRUITMENT_OWNER
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
                            name = "RECRUITMENT_NOT_FOUND",
                            value = StudySwaggerErrorExample.Recruitment.RECRUITMENT_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun cancelApplication(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @PathVariable recruitmentId: Long
    ): ResponseEntity<Unit>

    @Operation(summary = "내 신청 목록 조회", description = "내가 신청한 스터디 모집 현황을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyRecruitmentResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.RECRUITMENT_LIST_RESPONSE)]
                )]
            ),
        ]
    )
    fun getMyRecruitments(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId
    ): ResponseEntity<StudyRecruitmentListResponse>

    @Operation(summary = "스터디 신청자 목록 조회 (스터디장)", description = "특정 스터디의 신청자 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyRecruitmentResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.RECRUITMENT_LIST_RESPONSE)]
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
                        ExampleObject(name = "STUDY_NOT_FOUND", value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND),
                    ]
                )]
            )
        ]
    )
    fun getStudyRecruitments(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long
    ): ResponseEntity<List<StudyRecruitmentResponse>>
}
