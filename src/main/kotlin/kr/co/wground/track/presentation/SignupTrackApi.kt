package kr.co.wground.track.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.track.docs.TrackSwaggerErrorExample
import kr.co.wground.track.docs.TrackSwaggerResponseExample
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.presentation.response.SignupTrackResolveResponse
import kr.co.wground.track.presentation.response.SignupTrackTypesResponse
import kr.co.wground.track.presentation.response.TrackCardinalsResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Track", description = "회원가입용 트랙 조회 API")
interface SignupTrackApi {

    @Operation(summary = "회원가입용 트랙 유형 조회", description = "운영진이 등록한 트랙 유형 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SignupTrackTypesResponse::class),
                    examples = [ExampleObject(name = "SIGNUP_TRACK_TYPES", value = TrackSwaggerResponseExample.SIGNUP_TRACK_TYPES)]
                )]
            )
        ]
    )
    fun getSignupTrackTypes(): ResponseEntity<SignupTrackTypesResponse>

    @Operation(summary = "회원가입용 기수 조회", description = "선택한 트랙 유형의 등록된 기수 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TrackCardinalsResponse::class),
                    examples = [ExampleObject(name = "SIGNUP_TRACK_CARDINALS", value = TrackSwaggerResponseExample.TRACK_CARDINALS)]
                )]
            )
        ]
    )
    fun getSignupCardinalsByType(
        @Parameter(description = "조회할 트랙 유형", example = "FE", required = true)
        trackType: TrackType
    ): ResponseEntity<TrackCardinalsResponse>

    @Operation(summary = "회원가입용 trackId 조회", description = "선택한 trackType과 cardinal에 해당하는 trackId를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SignupTrackResolveResponse::class),
                    examples = [ExampleObject(name = "SIGNUP_TRACK_RESOLVE", value = TrackSwaggerResponseExample.SIGNUP_TRACK_RESOLVE)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "잘못된 입력",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
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
    fun resolveSignupTrack(
        @Parameter(description = "조회할 트랙 유형", example = "FE", required = true)
        trackType: TrackType,
        @Parameter(description = "조회할 기수", example = "3", required = true)
        cardinal: Int
    ): ResponseEntity<SignupTrackResolveResponse>
}
