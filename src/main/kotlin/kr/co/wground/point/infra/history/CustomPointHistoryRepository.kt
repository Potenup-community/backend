package kr.co.wground.point.infra.history

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition
import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointReferenceType
import kr.co.wground.point.domain.PointType
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

    //스터디 멤버들 조회
    fun findUserIdsWithHistory(
        userIds: List<UserId>,
        refType: PointReferenceType,
        refId: Long,
        type: PointType
    ): List<UserId>

    fun findHistoryByUserId(condition: PointHistoryQueryCondition): Slice<PointHistory>
}