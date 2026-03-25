package kr.co.wground.shop.application.query

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.InventoryItemDto

interface InventoryQueryUseCase {
    fun getMyInventory(userId: UserId): List<InventoryItemDto>
}