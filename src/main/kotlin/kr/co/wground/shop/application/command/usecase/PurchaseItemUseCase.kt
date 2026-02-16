package kr.co.wground.shop.application.command.usecase

import kr.co.wground.global.common.UserId

interface PurchaseItemUseCase {
    fun purchase(userId: UserId, itemId: Long)
}