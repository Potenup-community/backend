package kr.co.wground.point.application.command.usecase

import kr.co.wground.global.common.UserId

interface PurchasePointUseCase {
    fun forPurchase(userId: UserId, amount: Long, itemId: Long)
    fun forUpgradePurchase(userId: UserId, amount: Long, itemId: Long)
}
