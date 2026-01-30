package kr.co.wground.study.presentation

import io.swagger.v3.oas.annotations.Operation
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
import kr.co.wground.study.presentation.request.schedule.ScheduleCreateRequest
import kr.co.wground.study.presentation.request.schedule.ScheduleUpdateRequest
import kr.co.wground.study.presentation.response.schedule.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleListResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleQueryResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleUpdateResponse
import kr.co.wground.user.docs.UserSwaggerErrorExample
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Study Schedule", description = "스터디 일정(차수) 관리 API (Admin)")
interface StudyScheduleApi {

    @Operation(summary = "스터디 일정 생성", description = "새로운 스터디 일정(차수)을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ScheduleCreateResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.SCHEDULE_CREATE_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_CANT_START_AFTER_END_DATE",
                            value = StudySwaggerErrorExample.Schedule.STUDY_CANT_START_AFTER_END_DATE
                        ),
                        ExampleObject(
                            name = "STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE",
                            value = StudySwaggerErrorExample.Schedule.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE
                        ),
                        ExampleObject(
                            name = "STUDY_MONTH_ILLEGAL_RANGE",
                            value = StudySwaggerErrorExample.Schedule.STUDY_MONTH_ILLEGAL_RANGE
                        ),
                        ExampleObject(
                            name = "SCHEDULE_OVERLAP_WITH_NEXT",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_OVERLAP_WITH_NEXT
                        ),
                        ExampleObject(
                            name = "DUPLICATE_SCHEDULE_MONTH",
                            value = StudySwaggerErrorExample.Schedule.DUPLICATE_SCHEDULE_MONTH
                        ),
                        ExampleObject(
                            name = "SCHEDULE_OVERLAP_WITH_PREVIOUS",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_OVERLAP_WITH_PREVIOUS
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
            )
        ]
    )
    fun createSchedule(@RequestBody request: ScheduleCreateRequest): ResponseEntity<ScheduleCreateResponse>

    @Operation(summary = "스터디 일정 수정", description = "스터디 일정(차수)을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "수정 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ScheduleUpdateResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.SCHEDULE_UPDATE_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "STUDY_CANT_START_AFTER_END_DATE",
                            value = StudySwaggerErrorExample.Schedule.STUDY_CANT_START_AFTER_END_DATE
                        ),
                        ExampleObject(
                            name = "STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE",
                            value = StudySwaggerErrorExample.Schedule.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE
                        ),
                        ExampleObject(
                            name = "SCHEDULE_OVERLAP_WITH_NEXT",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_OVERLAP_WITH_NEXT
                        ),
                        ExampleObject(
                            name = "SCHEDULE_OVERLAP_WITH_PREVIOUS",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_OVERLAP_WITH_PREVIOUS
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
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                        ExampleObject(
                            name = "TRACK_NOT_FOUND",
                            value = StudySwaggerErrorExample.StudyTrack.TRACK_NOT_FOUND
                        ),
                    ]
                )]
            )
        ]
    )
    fun updateSchedule(
        @RequestBody request: ScheduleUpdateRequest,
        @PathVariable id: Long
    ): ResponseEntity<ScheduleUpdateResponse>

    @Operation(summary = "스터디 일정 삭제", description = "스터디 일정(차수)을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "409", description = "삭제 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "CANNOT_DELETE_SCHEDULE_WITH_STUDIES",
                            value = StudySwaggerErrorExample.Schedule.CANNOT_DELETE_SCHEDULE_WITH_STUDIES
                        ),
                    ]
                )]
            )
        ]
    )
    fun deleteSchedule(@PathVariable id: Long): ResponseEntity<Unit>

    @Operation(summary = "스터디 일정 조회", description = "해당 유저가 속한 트랙의 스터디 일정(차수)을 조회")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ScheduleListResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.SCHEDULE_LIST_QUERY_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                        ExampleObject(
                            name = "USER_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND
                        ),
                    ]
                )]
            ),
        ]
    )
    fun getSchedules(userId: CurrentUserId): ResponseEntity<ScheduleListResponse>

    @Operation(summary = "내 과정에서 현재 진행중인 스터디 일정 조회", description = "해당 유저가 속한 트랙의 스터디 일정(차수)을 조회")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ScheduleListResponse::class),
                    examples = [ExampleObject(value = StudySwaggerResponseExample.STUDY_SCHEDULE_RESPONSE)]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "SCHEDULE_NOT_FOUND",
                            value = StudySwaggerErrorExample.Schedule.SCHEDULE_NOT_FOUND
                        ),
                    ]
                )]
            ),
        ]
    )
    fun getCurrentSchedule(userId: CurrentUserId): ResponseEntity<ScheduleQueryResponse>
}
