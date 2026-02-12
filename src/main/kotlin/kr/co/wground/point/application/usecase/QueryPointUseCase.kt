package kr.co.wground.point.application.usecase

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.dto.PointBalanceDto
import kr.co.wground.point.application.dto.PointHistorySummaryDto
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.dto.PointTypeStatsDto
import kr.co.wground.point.infra.history.PointHistoryRepository
import kr.co.wground.point.infra.wallet.PointWalletRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional(readOnly = true)
class QueryPointUseCase(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointWalletRepository: PointWalletRepository
) {
    fun getBalance(userId: UserId): PointBalanceDto {
        val wallet = pointWalletRepository.findByUserId(userId)
            ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)

        return PointBalanceDto.from(wallet)
    }

    fun getHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto> {
        return pointHistoryRepository.findByUserId(userId, pageable)
            .map { PointHistorySummaryDto.from(it) }
    }

    fun getHistoryByType(userId: UserId, type: PointType, pageable: Pageable): Slice<PointHistorySummaryDto> {
        return pointHistoryRepository.findByUserIdAndType(userId, type, pageable)
            .map { PointHistorySummaryDto.from(it) }
    }

    fun getEarnedHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto> {
        return pointHistoryRepository.findEarnedByUserId(userId, pageable)
            .map { PointHistorySummaryDto.from(it) }
    }

    fun getUsedHistory(userId: UserId, pageable: Pageable): Slice<PointHistorySummaryDto> {
        return pointHistoryRepository.findUsedByUserId(userId, pageable)
            .map { PointHistorySummaryDto.from(it) }
    }

    fun getStatsByType(userId: UserId): List<PointTypeStatsDto> {
        return pointHistoryRepository.findStatsByUserIdGroupByType(userId)
    }

    fun getSumAmountByPeriod(userId: UserId, start: LocalDateTime, end: LocalDateTime, earnedOnly: Boolean = false): Long {
        return pointHistoryRepository.sumAmountByUserIdAndPeriod(userId, start, end, earnedOnly)
    }
}