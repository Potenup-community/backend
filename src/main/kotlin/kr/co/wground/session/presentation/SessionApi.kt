package kr.co.wground.session.presentation

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
import kr.co.wground.session.docs.SessionSwaggerErrorExample
import kr.co.wground.session.docs.SessionSwaggerResponseExample
import kr.co.wground.session.presentation.response.SessionListResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication

@Tag(name = "Session", description = "로그인 세션 관리 API")
interface SessionApi {

    @Operation(
        summary = "내 세션 목록 조회",
        description = "현재 유저의 모든 활성 세션 목록을 반환합니다. isCurrent=true인 항목이 현재 요청의 세션입니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionListResponse::class),
                    examples = [ExampleObject(name = "SESSION_LIST", value = SessionSwaggerResponseExample.SESSION_LIST)],
                )],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (액세스 토큰 없음 또는 만료)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_INACTIVE", value = SessionSwaggerErrorExample.Unauthorized.SESSION_INACTIVE)],
                )],
            ),
        ],
    )
    fun getSessions(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 액세스 토큰",
            schema = Schema(type = "string", example = "token_value"),
        )
        authentication: Authentication,
    ): ResponseEntity<SessionListResponse>

    @Operation(
        summary = "특정 세션 로그아웃",
        description = "지정한 sessionId에 해당하는 디바이스를 로그아웃합니다. 본인 소유 세션만 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "세션 철회 성공"),
            ApiResponse(
                responseCode = "400",
                description = "이미 비활성화된 세션",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_ALREADY_INACTIVE", value = SessionSwaggerErrorExample.BadRequest.SESSION_ALREADY_INACTIVE)],
                )],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (액세스 토큰 없음 또는 만료)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_INACTIVE", value = SessionSwaggerErrorExample.Unauthorized.SESSION_INACTIVE)],
                )],
            ),
            ApiResponse(
                responseCode = "403",
                description = "본인 소유 세션이 아님",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_FORBIDDEN", value = SessionSwaggerErrorExample.Forbidden.SESSION_FORBIDDEN)],
                )],
            ),
            ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_NOT_FOUND", value = SessionSwaggerErrorExample.NotFound.SESSION_NOT_FOUND)],
                )],
            ),
        ],
    )
    fun revokeSession(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 액세스 토큰",
            schema = Schema(type = "string", example = "token_value"),
        )
        authentication: Authentication,
        @Parameter(description = "철회할 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        sessionId: String,
    ): ResponseEntity<Unit>

    @Operation(
        summary = "다른 기기 전체 로그아웃",
        description = "현재 세션을 제외한 모든 활성 세션을 철회합니다. 세션이 없어도 성공으로 응답합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "다른 기기 전체 로그아웃 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (액세스 토큰 없음 또는 만료)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "SESSION_INACTIVE", value = SessionSwaggerErrorExample.Unauthorized.SESSION_INACTIVE)],
                )],
            ),
        ],
    )
    fun revokeOtherSessions(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 액세스 토큰",
            schema = Schema(type = "string", example = "token_value"),
        )
        authentication: Authentication,
    ): ResponseEntity<Unit>
}
