package kr.co.wground.point.infra.history

import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.ReferenceType
import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryRepository : JpaRepository<PointHistory, Long>, CustomPointHistoryRepository {

    fun existsByUserIdAndRefTypeAndRefIdAndType(
        userId: UserId,
        refType: ReferenceType,
        refId: Long,
        type: PointType
    ): Boolean

    fun countByUserId(userId: UserId): Long

    fun countByUserIdAndType(userId: UserId, type: PointType): Long
}