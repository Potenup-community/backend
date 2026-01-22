package kr.co.wground.user.presentation

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
import kr.co.wground.user.docs.UserSwaggerErrorExample
import kr.co.wground.user.docs.UserSwaggerResponseExample
import kr.co.wground.user.presentation.request.SignUpRequest
import kr.co.wground.user.presentation.response.UserResponse
import kr.co.wground.user.presentation.response.UserSummaryResponse
import org.springframework.http.ResponseEntity

@Tag(name = "User", description = "일반 유저 API")
interface UserApi {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 유저의 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserResponse::class),
                    examples = [ExampleObject(name = "MY_INFO", value = UserSwaggerResponseExample.MY_INFO)]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "유저 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "비활성화 된 유저",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "INACTIVE_USER", value = UserSwaggerErrorExample.BadRequest.INACTIVE_USER)
                    ]
                )]
            )
        ]
    )
    fun getMyInfo(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId
    ): ResponseEntity<UserResponse>

    @Operation(summary = "회원 가입 요청", description = "회원 가입을 요청합니다. 승인 대기 상태가 됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "요청 성공"),
            ApiResponse(
                responseCode = "400", description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ALREADY_SIGNED_USER",
                            value = UserSwaggerErrorExample.BadRequest.ALREADY_SIGNED_USER
                        ),
                        ExampleObject(
                            name = "REQUEST_SIGNUP_ALREADY_EXISTED",
                            value = UserSwaggerErrorExample.BadRequest.REQUEST_SIGNUP_ALREADY_EXISTED
                        ),
                        ExampleObject(
                            name = "DUPLICATED_PHONE_NUMBER",
                            value = UserSwaggerErrorExample.BadRequest.DUPLICATED_PHONE_NUMBER
                        )
                    ]
                )]
            )
        ]
    )
    fun requestSignUp(requestSignup: SignUpRequest): ResponseEntity<Unit>

    @Operation(summary = "멘션용 유저 목록 조회", description = "멘션을 위한 회원 요약 정보를 이름순으로 조회합니다. 커서 기반 페이지네이션을 지원합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserSummaryResponse::class),
                    examples = [ExampleObject(
                        name = "USER_SUMMARIES",
                        value = UserSwaggerResponseExample.MENTION_USER_LIST
                    )]
                )]
            )
        ]
    )
    fun getUsersForMention(
        @Parameter(description = "조회할 유저 수 (기본값: 20)", example = "20")
        size: Int,
        @Parameter(description = "커서: 마지막으로 조회한 유저 이름", example = "홍길동")
        cursorName: String?,
        @Parameter(description = "커서: 마지막으로 조회한 유저 ID", example = "123")
        cursorId: Long?
    ): ResponseEntity<List<UserSummaryResponse>>
}
