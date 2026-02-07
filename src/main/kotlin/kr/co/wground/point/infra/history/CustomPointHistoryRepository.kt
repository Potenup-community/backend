package kr.co.wground.point.infra.history

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.infra.dto.PointTypeStatsDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomPointHistoryRepository {
    // 여러 타입의 일일 카운트
    fun countByUserIdAndTypeInDay(
        userId: UserId,
        types: List<PointType>,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Long

    // 기간 내 총 금액
    fun sumAmountByUserIdAndPeriod(
        userId: UserId,
        start: LocalDateTime,
        end: LocalDateTime,
        earnedOnly: Boolean = false
    ): Long

    // 타입별 통계
    fun findStatsByUserIdGroupByType(userId: UserId): List<PointTypeStatsDto>

    // 일일 한도 체크 (단일 타입)
    fun countDailyByUserIdAndType(
        userId: UserId,
        type: PointType,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Long

    // 내역 조회 (전체)
    fun findByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory>

    // 내역 조회 (타입 필터)
    fun findByUserIdAndType(userId: UserId, type: PointType, pageable: Pageable): Slice<PointHistory>

    // 적립 내역만 (amount > 0)
    fun findEarnedByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory>

    // 사용 내역만 (amount < 0)
    fun findUsedByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory>
}