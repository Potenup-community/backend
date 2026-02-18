package kr.co.wground.point.infra.history

import com.querydsl.core.types.dsl.BooleanExpression
import kr.co.wground.point.application.query.dto.PointHistoryFilter
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.QPointHistory.pointHistory
import org.springframework.stereotype.Component

@Component
class HistoryExpression {

    fun resolveCondition(condition: PointHistoryQueryCondition): BooleanExpression? {
        return when (condition.filter) {
            PointHistoryFilter.ALL -> null
            PointHistoryFilter.USED -> pointHistory.type.eq(PointType.USE_SHOP)
            PointHistoryFilter.EARNED -> pointHistory.type.ne(PointType.USE_SHOP)
        }
    }
}