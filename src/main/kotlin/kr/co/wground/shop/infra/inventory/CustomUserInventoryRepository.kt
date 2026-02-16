package kr.co.wground.shop.infra.inventory

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.InventoryItemDto

interface CustomUserInventoryRepository {
    fun findActiveWithItemByUserId(userId: UserId): List<InventoryItemDto>
}