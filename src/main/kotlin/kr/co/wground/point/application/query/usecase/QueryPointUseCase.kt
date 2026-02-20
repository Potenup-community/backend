package kr.co.wground.point.application.query.usecase

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.query.dto.PointBalanceDto
import kr.co.wground.point.application.query.dto.PointHistorySummaryDto
import org.springframework.data.domain.Slice
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition
import kr.co.wground.point.application.query.dto.PointTypeStatsDto

interface QueryPointUseCase {
    fun getBalance(userId: UserId): PointBalanceDto
    fun findHistory(condition: PointHistoryQueryCondition): Slice<PointHistorySummaryDto>
    fun getSumAmountByPeriod(userId: UserId, start: LocalDateTime, end: LocalDateTime, earnedOnly: Boolean): Long
    fun getStatsByType(userId: UserId): List<PointTypeStatsDto>
}
