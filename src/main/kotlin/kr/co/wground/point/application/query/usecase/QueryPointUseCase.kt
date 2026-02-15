package kr.co.wground.point.application.query.usecase

import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.query.dto.PointBalanceDto
import kr.co.wground.point.application.query.dto.PointHistorySummaryDto
import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.domain.PointType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import java.time.LocalDateTime

interface QueryPointUseCase {
    fun getBalance(userId: UserId): PointBalanceDto
    fun getHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto>
    fun getHistoryByType(userId: UserId, type: PointType, pageable: Pageable): Slice<PointHistorySummaryDto>
    fun getEarnedHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto>
    fun getUsedHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto>
    fun getStatsByType(userId: UserId): List<PointTypeStatsDto>
    fun getSumAmountByPeriod(userId: UserId, start: LocalDateTime, end: LocalDateTime, earnedOnly: Boolean = false): Long
}
