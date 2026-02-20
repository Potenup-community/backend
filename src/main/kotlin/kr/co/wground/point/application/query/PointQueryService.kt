package kr.co.wground.point.application.query

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.query.dto.PointBalanceDto
import kr.co.wground.point.application.query.dto.PointHistorySummaryDto
import kr.co.wground.point.application.query.usecase.QueryPointUseCase
import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition

@Service
@Transactional(readOnly = true)
class PointQueryService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) : QueryPointUseCase {

    override fun getBalance(userId: UserId): PointBalanceDto {
        val wallet = pointWalletRepository.findByUserId(userId)
            ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)

        return PointBalanceDto.from(wallet)
    }

    override fun getSumAmountByPeriod(
        userId: UserId,
        start: LocalDateTime,
        end: LocalDateTime,
        earnedOnly: Boolean
    ): Long {
        return pointHistoryRepository.sumAmountByUserIdAndPeriod(userId, start, end, earnedOnly)
    }

    override fun getStatsByType(userId: UserId): List<PointTypeStatsDto> {
        return pointHistoryRepository.findStatsByUserIdGroupByType(userId)
    }

    override fun findHistory(condition: PointHistoryQueryCondition): Slice<PointHistorySummaryDto> {
        return pointHistoryRepository.findHistoryByUserId(condition).map { PointHistorySummaryDto.from(it) }
    }
}
