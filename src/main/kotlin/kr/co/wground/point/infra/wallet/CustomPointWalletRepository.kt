package kr.co.wground.point.infra.wallet

import kr.co.wground.global.common.UserId

interface CustomPointWalletRepository {
    fun addBalance(userId: UserId, amount: Long): Long
}