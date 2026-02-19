package kr.co.wground.point.application.query.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointType
import org.springframework.data.domain.Pageable

data class PointHistoryQueryCondition(
    val userId: UserId,
    val filter: PointHistoryFilter = PointHistoryFilter.ALL,
    val type: PointType? = null,
    val pageable: Pageable
){
    companion object {
        fun of(
            userId: UserId,
            filter: PointHistoryFilter,
            type: PointType?,
            pageable: Pageable
        ): PointHistoryQueryCondition {
            return PointHistoryQueryCondition(
                userId = userId,
                filter = filter,
                type = if (filter == PointHistoryFilter.ALL) type else null,
                pageable = pageable
            )
        }
    }
}