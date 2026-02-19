package kr.co.wground.point.presentation.response

import kr.co.wground.point.application.query.dto.PointBalanceDto
import java.time.LocalDateTime

data class PointBalanceResponse(
    val balance: Long,
    val lastUpdatedAt: LocalDateTime
) {
    companion object {
        fun from(dto: PointBalanceDto): PointBalanceResponse {
            return PointBalanceResponse(
                balance = dto.balance,
                lastUpdatedAt = dto.lastUpdatedAt
            )
        }
    }
}