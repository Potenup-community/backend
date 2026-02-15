package kr.co.wground.point.infra.dto

import kr.co.wground.point.domain.PointType

data class PointTypeStatsDto(
    val type: PointType,
    val count: Long,
    val totalAmount: Long
)
