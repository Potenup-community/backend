package kr.co.wground.point.presentation.response

import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.domain.PointType

data class PointTypeStatsResponse(
    val type: PointType,
    val description: String,
    val count: Long,
    val totalAmount: Long
) {
    companion object {
        fun from(dto: PointTypeStatsDto): PointTypeStatsResponse {
            return PointTypeStatsResponse(
                type = dto.type,
                description = dto.type.description,
                count = dto.count,
                totalAmount = dto.totalAmount
            )
        }
    }
}