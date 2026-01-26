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
import kr.co.wground.global.jwt.UserPrincipal
import kr.co.wground.study.application.dto.StudySearchCondition
import kr.co.wground.study.docs.StudySwaggerErrorExample
import kr.co.wground.study.docs.StudySwaggerResponseExample
import kr.co.wground.study.presentation.request.study.StudyCreateRequest
import kr.co.wground.study.presentation.request.study.StudyUpdateRequest
import kr.co.wground.study.presentation.response.CustomSliceResponse
import kr.co.wground.study.presentation.response.study.StudyDetailResponse
import kr.co.wground.study.presentation.response.study.StudyIdResponse
import kr.co.wground.study.presentation.response.study.StudyQueryResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Study", description = "스터디 API")
interface StudyApi {

    @Operation(summary = "스터디 생성", description = "새로운 스터디를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyIdResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_ID_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_NAME_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_NAME_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_DESCRIPTION_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_DESCRIPTION_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_CAPACITY_TOO_SMALL",
                            value = StudySwaggerErrorExample.Study.STUDY_CAPACITY_TOO_SMALL
                        ),
                        ExampleObject(
                            name = "STUDY_CAPACITY_TOO_BIG",
                            value = StudySwaggerErrorExample.Study.STUDY_CAPACITY_TOO_BIG
                        ),
                        ExampleObject(
                            name = "STUDY_URL_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_URL_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_TAG_COUNT_EXCEEDED",
                            value = StudySwaggerErrorExample.Study.STUDY_TAG_COUNT_EXCEEDED
                        ),
                        ExampleObject(
                            name = "TAG_LENGTH_INVALID_RANGE",
                            value = StudySwaggerErrorExample.Tag.TAG_LENGTH_INVALID_RANGE
                        ),
                        ExampleObject(
                            name = "TAG_FORMAT_INVALID",
                            value = StudySwaggerErrorExample.Tag.TAG_FORMAT_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_NOT_RECRUITING",
                            value = StudySwaggerErrorExample.Study.STUDY_NOT_RECRUITING
                        ),
                        ExampleObject(
                            name = "TRACK_IS_NOT_ENROLLED",
                            value = StudySwaggerErrorExample.StudyTrack.TRACK_IS_NOT_ENROLLED
                        ),
                        ExampleObject(
                            name = "NO_CURRENT_SCHEDULE",
                            value = StudySwaggerErrorExample.Schedule.NO_CURRENT_SCHEDULE
                        ),
                        ExampleObject(
                            name = "MAX_STUDY_EXCEEDED",
                            value = StudySwaggerErrorExample.Study.MAX_STUDY_EXCEEDED
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
                            name = "TRACK_NOT_FOUND",
                            value = StudySwaggerErrorExample.StudyTrack.TRACK_NOT_FOUND
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "409", description = "충돌",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TAG_CREATION_FAIL",
                            value = StudySwaggerErrorExample.Tag.TAG_CREATION_FAIL
                        ),
                    ]
                )]
            )
        ]
    )
    fun createStudy(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @RequestBody @Valid request: StudyCreateRequest
    ): ResponseEntity<StudyIdResponse>

    @Operation(summary = "스터디 상세 조회", description = "스터디 ID로 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StudyDetailResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_DETAIL_RESPONSE)]
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
    fun getStudy(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long
    ): ResponseEntity<StudyDetailResponse>

    @Operation(summary = "스터디 수정", description = "스터디 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "수정 성공"),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_CANNOT_MODIFY_AFTER_DETERMINED",
                            value = StudySwaggerErrorExample.Study.STUDY_CANNOT_MODIFY_AFTER_DETERMINED
                        ),
                        ExampleObject(
                            name = "STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT",
                            value = StudySwaggerErrorExample.Study.STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT
                        ),
                        ExampleObject(
                            name = "STUDY_NAME_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_NAME_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_DESCRIPTION_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_DESCRIPTION_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_URL_INVALID",
                            value = StudySwaggerErrorExample.Study.STUDY_URL_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_TAG_COUNT_EXCEEDED",
                            value = StudySwaggerErrorExample.Study.STUDY_TAG_COUNT_EXCEEDED
                        ),
                        ExampleObject(
                            name = "TAG_LENGTH_INVALID_RANGE",
                            value = StudySwaggerErrorExample.Tag.TAG_LENGTH_INVALID_RANGE
                        ),
                        ExampleObject(
                            name = "TAG_FORMAT_INVALID",
                            value = StudySwaggerErrorExample.Tag.TAG_FORMAT_INVALID
                        ),
                        ExampleObject(
                            name = "STUDY_CANNOT_MODIFY_AFTER_DEADLINE",
                            value = StudySwaggerErrorExample.Study.STUDY_CANNOT_MODIFY_AFTER_DEADLINE
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
                        ExampleObject(name = "STUDY_NOT_FOUND", value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND),
                    ]
                )]
            )
        ]
    )
    fun updateStudy(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @PathVariable studyId: Long,
        @RequestBody @Valid request: StudyUpdateRequest
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 삭제", description = "스터디를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "400", description = "삭제 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_CANT_DELETE_STATUS_DETERMINE",
                            value = StudySwaggerErrorExample.Study.STUDY_CANT_DELETE_STATUS_DETERMINE
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
                        ExampleObject(name = "STUDY_NOT_FOUND", value = StudySwaggerErrorExample.Study.STUDY_NOT_FOUND),
                    ]
                )]
            )
        ]
    )
    fun deleteStudy(
        @Parameter(hidden = true) userPrincipal: UserPrincipal,
        @PathVariable studyId: Long
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 승인 (관리자)", description = "관리자가 스터디 개설을 승인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "승인 성공"),
            ApiResponse(
                responseCode = "400", description = "승인 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_MUST_BE_CLOSED_TO_APPROVE",
                            value = StudySwaggerErrorExample.Study.STUDY_MUST_BE_CLOSED_TO_APPROVE
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
    fun approveStudy(
        @PathVariable studyId: Long
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 반려 (관리자)", description = "관리자가 스터디 개설을 반려합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "반려 성공"),
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
    fun rejectStudy(
        @PathVariable studyId: Long
    ): ResponseEntity<Unit>

    @Operation(summary = "스터디 검색/조회", description = "조건에 따라 스터디를 검색하고 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CustomSliceResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_SEARCH_RESPONSE)]
                )]
            ),
        ]
    )
    fun searchStudies(
        @ParameterObject @ModelAttribute condition: StudySearchCondition,
        @ParameterObject @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId
    ): ResponseEntity<CustomSliceResponse<StudyQueryResponse>>
}
