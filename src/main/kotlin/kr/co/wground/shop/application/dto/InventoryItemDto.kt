package kr.co.wground.shop.application.dto

import java.time.LocalDateTime
import kr.co.wground.shop.domain.constant.ShopItemType

data class InventoryItemDto(
    val inventoryId: Long,
    val shopItemId: Long,
    val name: String,
    val description: String,
    val itemType: ShopItemType,
    val imageUrl: String,
    val consumable: Boolean,
    val equipped: Boolean,
    val expireAt: LocalDateTime?,
)
