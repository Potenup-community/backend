package kr.co.wground.user.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.user.docs.UserSwaggerErrorExample
import kr.co.wground.user.docs.UserSwaggerResponseExample
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.AuthStatusResponse
import kr.co.wground.user.presentation.response.RoleResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth", description = "인증/인가 API")
interface AuthApi {

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다. 성공 시 Access/Refresh Token이 쿠키에 설정됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = RoleResponse::class),
                    examples = [ExampleObject(name = "LOGIN_SUCCESS", value = UserSwaggerResponseExample.LOGIN_SUCCESS)]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "로그인 실패 (비밀번호 불일치 또는 비활성 유저)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "INACTIVE_USER", value = UserSwaggerErrorExample.BadRequest.INACTIVE_USER)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND)
                    ]
                )]
            )
        ]
    )
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<RoleResponse>

    @Operation(summary = "로그아웃", description = "로그아웃 처리하며 쿠키를 만료시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            ApiResponse(
                responseCode = "404",
                description = "유저 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND)
                    ]
                )]
            )
        ]
    )
    fun logout(@Parameter(hidden = true) userId: CurrentUserId): ResponseEntity<Unit>

    @Operation(summary = "인증 상태 확인", description = "현재 요청의 인증 상태(로그인 여부, 유저 ID, 권한)를 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AuthStatusResponse::class),
                    examples = [ExampleObject(name = "AUTH_STATUS", value = UserSwaggerResponseExample.AUTH_STATUS)]
                )]
            )
        ]
    )
    fun getAuthStatus(@Parameter(hidden = true) authentication: Authentication): ResponseEntity<AuthStatusResponse>
}
