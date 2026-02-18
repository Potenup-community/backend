package kr.co.wground.point.presentation

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
import kr.co.wground.point.presentation.request.AdminGivePointRequest
import org.springframework.http.ResponseEntity

@Tag(name = "Admin - Points", description = "관리자 포인트 API")
interface AdminPointApi {

    @Operation(
        summary = "포인트 직접 지급",
        description = """관리자가 특정 사용자에게 포인트를 직접 지급합니다.
   
  - 대상 사용자의 지갑이 없으면 자동 생성됩니다.
  - amount는 1 이상이어야 합니다.
  - 동시성 충돌 시 최대 3회 재시도 후 실패합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "지급 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_AMOUNT",
                            value = PointSwaggerErrorExample.BadRequest.INVALID_AMOUNT
                        ),
                        ExampleObject(
                            name = "INVALID_USER_ID",
                            value = PointSwaggerErrorExample.BadRequest.INVALID_USER_ID
                        ),
                        ExampleObject(
                            name = "INVALID_INPUT",
                            value = PointSwaggerErrorExample.BadRequest.INVALID_INPUT
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "관리자 권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ACCESS_DENIED",
                            value = PointSwaggerErrorExample.Forbidden.ACCESS_DENIED
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "포인트 처리 실패 (동시성 충돌 재시도 초과)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "POINT_PROCESSING_FAILED",
                            value = PointSwaggerErrorExample.ServerError.POINT_PROCESSING_FAILED
                        )
                    ]
                )]
            ),
        ]
    )
    fun givePoint(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "관리자 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) adminId: CurrentUserId,
        request: AdminGivePointRequest,
    ): ResponseEntity<Unit>
}