package kr.co.wground.point.application.dto

import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.ReferenceType
import java.time.LocalDateTime

data class PointHistorySummaryDto(
    val id: Long,
    val amount: Long,
    val type: PointType,
    val description: String,
    val refType: ReferenceType,
    val refId: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(history: PointHistory): PointHistorySummaryDto {
            return PointHistorySummaryDto(
                id = history.id!!,
                amount = history.amount,
                type = history.type,
                description = history.type.description,
                refType = history.refType,
                refId = history.refId,
                createdAt = history.createdAt
            )
        }
    }
}