package kr.co.wground.user.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.user.application.operations.dto.AdminSearchUserDto
import kr.co.wground.user.docs.UserSwaggerErrorExample
import kr.co.wground.user.docs.UserSwaggerResponseExample
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.MultipleDecisionRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.UserCountResponse
import kr.co.wground.user.presentation.response.UserPageResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@Tag(name = "Admin", description = "관리자 API")
interface AdminApi {

    @Operation(summary = "가입 승인/거절 (단건)", description = "특정 유저의 가입 요청을 승인하거나 거절합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "처리 성공"),
            ApiResponse(
                responseCode = "404", description = "유저 또는 요청 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "REQUEST_SIGNUP_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.REQUEST_SIGNUP_NOT_FOUND
                        ),
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND)
                    ]
                )]
            )
        ]
    )
    fun decisionSignUp(
        @Parameter(description = "대상 유저 ID", example = "1") userId: UserId,
        request: DecisionStatusRequest
    ): ResponseEntity<Unit>

    @Operation(summary = "가입 승인/거절 (다건)", description = "여러 유저의 가입 요청을 일괄 승인하거나 거절합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "처리 성공"),
            ApiResponse(
                responseCode = "404", description = "유저 또는 요청 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "REQUEST_SIGNUP_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.REQUEST_SIGNUP_NOT_FOUND
                        ),
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND)
                    ]
                )]
            )
        ]
    )
    fun multipleDecision(request: MultipleDecisionRequest): ResponseEntity<Unit>

    @Operation(summary = "유저 목록 조회", description = "조건에 따라 유저 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserPageResponse::class),
                    examples = [ExampleObject(name = "USER_LIST", value = UserSwaggerResponseExample.USER_LIST)]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류 (페이지 번호 등)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "PAGE_NUMBER_MIN_ERROR",
                            value = UserSwaggerErrorExample.BadRequest.PAGE_NUMBER_MIN_ERROR
                        ),
                        ExampleObject(
                            name = "PAGE_NUMBER_IS_OVER_TOTAL_PAGE",
                            value = UserSwaggerErrorExample.BadRequest.PAGE_NUMBER_IS_OVER_TOTAL_PAGE
                        ),
                        ExampleObject(
                            name = "CANT_REQUEST_NEXT_PAGE_IN_ZERO_ELEMENT",
                            value = UserSwaggerErrorExample.BadRequest.CANT_REQUEST_NEXT_PAGE_IN_ZERO_ELEMENT
                        )
                    ]
                )]
            )
        ]
    )
    fun getAllUsers(
        @ParameterObject condition: UserSearchRequest,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<UserPageResponse<AdminSearchUserDto>>

    @Operation(summary = "유저 수 조회", description = "조건에 해당하는 유저 수를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserCountResponse::class),
                    examples = [ExampleObject(name = "USER_COUNT", value = UserSwaggerResponseExample.USER_COUNT)]
                )]
            )
        ]
    )
    fun getUserCount(@ParameterObject condition: UserSearchRequest): ResponseEntity<UserCountResponse>
}