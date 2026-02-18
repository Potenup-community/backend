package kr.co.wground.point.presentation

import java.time.LocalDateTime
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.point.application.query.dto.PointHistoryFilter
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition
import kr.co.wground.point.application.query.usecase.QueryPointUseCase
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.presentation.response.PointBalanceResponse
import kr.co.wground.point.presentation.response.PointHistoriesResponse
import kr.co.wground.point.presentation.response.PointPeriodSummaryResponse
import kr.co.wground.point.presentation.response.PointTypeStatsResponse
import kr.co.wground.point.presentation.response.toResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/points")
class PointController(
    private val queryPointUseCase: QueryPointUseCase,
) : PointApi {

    @GetMapping("/balance")
    override fun getBalance(userId: CurrentUserId): ResponseEntity<PointBalanceResponse> {
        val balance = queryPointUseCase.getBalance(userId.value)
        return ResponseEntity.ok(PointBalanceResponse.from(balance))
    }

    @GetMapping("/history")
    override fun getHistory(
        userId: CurrentUserId,
        @RequestParam(defaultValue = "ALL") filter: PointHistoryFilter,
        @RequestParam(required = false) type: PointType?,
        @PageableDefault(size = 20)
        @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): ResponseEntity<PointHistoriesResponse> {
        val result = queryPointUseCase.findHistory(
            PointHistoryQueryCondition.of(
                userId = userId.value,
                filter = filter,
                type = type,
                pageable = pageable
            )
        )
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/summary")
    override fun getSummary(
        userId: CurrentUserId,
        @RequestParam start: LocalDateTime,
        @RequestParam end: LocalDateTime,
        @RequestParam(defaultValue = "false") earnedOnly: Boolean,
    ): ResponseEntity<PointPeriodSummaryResponse> {
        val total = queryPointUseCase.getSumAmountByPeriod(userId.value, start, end, earnedOnly)
        return ResponseEntity.ok(PointPeriodSummaryResponse(total))
    }

    @GetMapping("/stats")
    override fun getStats(
        userId: CurrentUserId,
    ): ResponseEntity<List<PointTypeStatsResponse>> {
        val stats = queryPointUseCase.getStatsByType(userId.value)
        return ResponseEntity.ok(stats.map { PointTypeStatsResponse.from(it) })
    }
}