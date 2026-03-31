package kr.co.wground.track.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.track.application.dto.TrackQueryDto
import kr.co.wground.track.docs.TrackSwaggerErrorExample
import kr.co.wground.track.docs.TrackSwaggerRequestExample
import kr.co.wground.track.docs.TrackSwaggerResponseExample
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.presentation.request.CreateTrackRequest
import kr.co.wground.track.presentation.request.UpdateTrackRequest
import kr.co.wground.track.presentation.response.TrackCardinalsResponse
import kr.co.wground.track.presentation.response.TrackListResponse
import kr.co.wground.track.presentation.response.TrackTypesResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Track", description = "과정(Track) 관리 API")
interface TrackApi {

    @Operation(summary = "과정 생성", description = "새로운 과정을 생성합니다. 생성 요청은 trackType/cardinal 기준이며 trackName은 저장하지 않습니다.")
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
                            name = "INVALID_TRACK_INPUT",
                            value = TrackSwaggerErrorExample.BadRequest.INVALID_TRACK_INPUT
                        )
                    ]
                )]
            )
        ]
    )
    fun createTrack(
        @RequestBody(
            description = "과정 생성 요청",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateTrackRequest::class),
                examples = [ExampleObject(name = "CREATE_TRACK", value = TrackSwaggerRequestExample.CREATE_TRACK)]
            )]
        )
        createTrack: CreateTrackRequest
    ): ResponseEntity<TrackListResponse<TrackQueryDto>>

    @Operation(summary = "과정 수정", description = "기존 과정을 수정합니다. 응답의 표시명은 trackType/cardinal 기준으로 계산됩니다.")
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
                            name = "INVALID_TRACK_INPUT",
                            value = TrackSwaggerErrorExample.BadRequest.INVALID_TRACK_INPUT
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
        @RequestBody(
            description = "과정 수정 요청",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateTrackRequest::class),
                examples = [ExampleObject(name = "UPDATE_TRACK", value = TrackSwaggerRequestExample.UPDATE_TRACK)]
            )]
        )
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

    @Operation(
        summary = "과정 목록 조회 (Admin 제외, 회원가입 용)",
        description = "관리자 트랙을 제외한 모든 과정을 조회합니다. trackType=ADMIN 기준으로 제외합니다."
    )
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
    fun getTracksExceptAdmin(
        @Parameter(description = "트랙 유형 필터(선택)", example = "FE", required = false)
        trackType: TrackType?
    ): ResponseEntity<TrackListResponse<TrackQueryDto>>

    @Operation(summary = "전체 과정 목록 조회 (Admin 전용)", description = "관리자 트랙(ADMIN)을 제외한 과정을 조회합니다.")
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
    fun getAllTracks(
        @Parameter(description = "트랙 유형 필터(선택)", example = "FE", required = false)
        trackType: TrackType?
    ): ResponseEntity<TrackListResponse<TrackQueryDto>>

    @Operation(summary = "트랙 등록용 유형 목록 조회", description = "트랙 등록 화면에서 사용할 트랙 유형 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackTypesResponse::class),
                    examples = [ExampleObject(name = "TRACK_TYPES", value = TrackSwaggerResponseExample.TRACK_TYPES)]
                )]
            )
        ]
    )
    fun getTrackTypesForRegistration(): ResponseEntity<TrackTypesResponse>

    @Operation(summary = "트랙 유형별 기수 목록 조회", description = "운영진이 등록한 특정 트랙 유형의 기수(cardinal) 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackCardinalsResponse::class),
                    examples = [ExampleObject(name = "TRACK_CARDINALS", value = TrackSwaggerResponseExample.TRACK_CARDINALS)]
                )]
            )
        ]
    )
    fun getTrackCardinalsByType(
        @Parameter(description = "조회할 트랙 유형", example = "FE", required = true)
        trackType: TrackType
    ): ResponseEntity<TrackCardinalsResponse>
}
