package kr.co.wground.track.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.track.application.dto.TrackQueryDto
import kr.co.wground.track.docs.TrackSwaggerErrorExample
import kr.co.wground.track.docs.TrackSwaggerResponseExample
import kr.co.wground.track.presentation.request.CreateTrackRequest
import kr.co.wground.track.presentation.request.UpdateTrackRequest
import kr.co.wground.track.presentation.response.TrackListResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Track", description = "과정(Track) 관리 API")
interface TrackApi {

    @Operation(summary = "과정 생성", description = "새로운 과정을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackListResponse::class),
                    examples = [ExampleObject(name = "TRACK_SINGLE", value = TrackSwaggerResponseExample.TRACK_LIST)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "잘못된 입력",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_DATE_RANGE",
                            value = TrackSwaggerErrorExample.BadRequest.INVALID_DATE_RANGE
                        ),
                        ExampleObject(
                            name = "TRACK_NAME_IS_BLANK",
                            value = TrackSwaggerErrorExample.BadRequest.TRACK_NAME_IS_BLANK
                        )
                    ]
                )]
            )
        ]
    )
    fun createTrack(createTrack: CreateTrackRequest): ResponseEntity<TrackListResponse<TrackQueryDto>>

    @Operation(summary = "과정 수정", description = "기존 과정을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "수정 성공"),
            ApiResponse(
                responseCode = "400", description = "잘못된 입력",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_DATE_RANGE",
                            value = TrackSwaggerErrorExample.BadRequest.INVALID_DATE_RANGE
                        ),
                        ExampleObject(
                            name = "TRACK_NAME_IS_BLANK",
                            value = TrackSwaggerErrorExample.BadRequest.TRACK_NAME_IS_BLANK
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "과정 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TRACK_NOT_FOUND",
                            value = TrackSwaggerErrorExample.NotFound.TRACK_NOT_FOUND
                        )
                    ]
                )]
            )
        ]
    )
    fun updateTrack(
        @Parameter(description = "수정할 과정 ID", example = "1") trackId: TrackId,
        updateTrack: UpdateTrackRequest
    ): ResponseEntity<Unit>

    @Operation(summary = "과정 삭제", description = "과정을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "404", description = "과정 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TRACK_NOT_FOUND",
                            value = TrackSwaggerErrorExample.NotFound.TRACK_NOT_FOUND
                        )
                    ]
                )]
            )
        ]
    )
    fun deleteTrack(@Parameter(description = "삭제할 과정 ID", example = "1") trackId: TrackId): ResponseEntity<Unit>

    @Operation(summary = "과정 목록 조회 (Admin 제외, 회원가입 용)", description = "관리자 트랙을 제외한 모든 과정을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackListResponse::class),
                    examples = [ExampleObject(name = "TRACK_LIST", value = TrackSwaggerResponseExample.TRACK_LIST_EXCEPT_ADMIN)]
                )]
            )
        ]
    )
    fun getTracksExceptAdmin(): ResponseEntity<TrackListResponse<TrackQueryDto>>

    @Operation(summary = "전체 과정 목록 조회 (Admin 전용)", description = "모든 과정을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackListResponse::class),
                    examples = [ExampleObject(name = "TRACK_LIST", value = TrackSwaggerResponseExample.TRACK_LIST)]
                )]
            )
        ]
    )
    fun getAllTracks(): ResponseEntity<TrackListResponse<TrackQueryDto>>
}
