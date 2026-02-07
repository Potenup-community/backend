package kr.co.wground.point.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointWallet
import java.time.LocalDateTime

data class PointBalanceDto(
    val userId: UserId,
    val balance: Long,
    val lastUpdatedAt: LocalDateTime
) {
    companion object {
        fun from(wallet: PointWallet): PointBalanceDto {
            return PointBalanceDto(
                userId = wallet.userId,
                balance = wallet.balance,
                lastUpdatedAt = wallet.lastUpdatedAt
            )
        }
    }
}