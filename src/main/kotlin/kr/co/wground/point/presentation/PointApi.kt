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
import kr.co.wground.point.docs.PointSwaggerResponseExample
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.presentation.response.PointBalanceResponse
import kr.co.wground.point.presentation.response.PointHistoriesResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@Tag(name = "Points", description = "포인트 API")
interface PointApi {

    @Operation(
        summary = "내 포인트 잔액 조회",
        description = "현재 로그인한 사용자의 포인트 잔액을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PointBalanceResponse::class),
                    examples = [
                        ExampleObject(name = "BALANCE", value = PointSwaggerResponseExample.BALANCE)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "포인트 지갑을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "WALLET_NOT_FOUND",
                            value = PointSwaggerErrorExample.NotFound.WALLET_NOT_FOUND
                        )
                    ]
                )]
            ),
        ]
    )
    fun getBalance(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<PointBalanceResponse>

    @Operation(
        summary = "포인트 내역 조회",
        description = """현재 로그인한 사용자의 포인트 내역을 페이지 단위로 조회합니다.
   
  - filter: ALL(전체), EARNED(적립만), USED(사용만)
  - type: 특정 PointType으로 필터링 (type이 지정되면 filter보다 우선 적용)
  - USE_SHOP 타입의 amount도 양수로 저장됩니다. type으로 적립/사용을 구분하세요."""
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PointHistoriesResponse::class),
                    examples = [
                        ExampleObject(name = "HISTORIES", value = PointSwaggerResponseExample.HISTORIES),
                        ExampleObject(name = "HISTORIES_EMPTY", value = PointSwaggerResponseExample.HISTORIES_EMPTY)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터 (filter 또는 type 값 오류)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_INPUT",
                            value = PointSwaggerErrorExample.BadRequest.INVALID_INPUT
                        )
                    ]
                )]
            ),
        ]
    )
    fun getHistory(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @Parameter(description = "필터 (ALL, EARNED, USED)", example = "ALL")
        filter: PointHistoryFilter,
        @Parameter(description = "포인트 타입으로 필터링 (선택, 지정 시 filter 무시)", example = "ATTENDANCE")
        type: PointType?,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PointHistoriesResponse>
}