package kr.co.wground.shop.infra.dto

import java.time.Duration
import java.time.LocalDateTime
import kr.co.wground.shop.domain.constant.ShopItemType

data class InventoryItemInfra(
    val inventoryId: Long,
    val shopItemId: Long,
    val name: String,
    val description: String,
    val itemType: ShopItemType,
    val imageUrl: String,
    val consumable: Boolean,
    val equipped: Boolean,
    val expireAt: LocalDateTime?,
) {
    fun remainDays(now: LocalDateTime): Long? {
        return expireAt?.let { Duration.between(now, it).toDays() }?.coerceAtLeast(0)
    }
}