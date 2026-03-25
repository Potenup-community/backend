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
import kr.co.wground.point.application.query.dto.PointHistoryFilter
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.presentation.response.PointBalanceResponse
import kr.co.wground.point.presentation.response.PointHistoriesResponse
import kr.co.wground.point.presentation.response.PointPeriodSummaryResponse
import kr.co.wground.point.presentation.response.PointTypeStatsResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

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
                    examples = [ExampleObject(name = "BALANCE", value = PointSwaggerResponseExample.BALANCE)]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "포인트 지갑을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "WALLET_NOT_FOUND", value = PointSwaggerErrorExample.NotFound.WALLET_NOT_FOUND)]
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
    - type: filter가 ALL일 때만 적용 (filter가 EARNED 또는 USED이면 type은 무시됩니다)
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
                    examples = [ExampleObject(name = "INVALID_INPUT", value = PointSwaggerErrorExample.BadRequest.INVALID_INPUT)]
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
        @Parameter(description = "포인트 타입으로 필터링 (선택, filter가 ALL일 때만 적용)", example = "ATTENDANCE")
        type: PointType?,
        @ParameterObject
        @PageableDefault(size = 20)
        @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): ResponseEntity<PointHistoriesResponse>

    @Operation(
        summary = "기간별 포인트 합계 조회",
        description = """지정한 기간 내 포인트 합계를 조회합니다.

    - earnedOnly: true이면 사용(USE_SHOP) 제외한 적립 합계만 반환
    - start, end는 ISO 8601 형식 (예: 2026-02-01T00:00:00)"""
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PointPeriodSummaryResponse::class),
                    examples = [ExampleObject(name = "PERIOD_SUMMARY", value = PointSwaggerResponseExample.PERIOD_SUMMARY)]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 날짜 형식",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "INVALID_INPUT", value = PointSwaggerErrorExample.BadRequest.INVALID_INPUT)]
                )]
            ),
        ]
    )
    fun getSummary(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @Parameter(description = "조회 시작 일시 (ISO 8601)", example = "2026-02-01T00:00:00")
        start: LocalDateTime,
        @Parameter(description = "조회 종료 일시 (ISO 8601)", example = "2026-02-28T23:59:59")
        end: LocalDateTime,
        @Parameter(description = "적립만 합산 여부 (USE_SHOP 제외)", example = "false")
        earnedOnly: Boolean,
    ): ResponseEntity<PointPeriodSummaryResponse>

    @Operation(
        summary = "타입별 포인트 통계 조회",
        description = "현재 로그인한 사용자의 포인트 타입별 횟수 및 합계를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(name = "TYPE_STATS", value = PointSwaggerResponseExample.TYPE_STATS),
                        ExampleObject(name = "TYPE_STATS_EMPTY", value = PointSwaggerResponseExample.TYPE_STATS_EMPTY)
                    ]
                )]
            ),
        ]
    )
    fun getStats(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<List<PointTypeStatsResponse>>
}