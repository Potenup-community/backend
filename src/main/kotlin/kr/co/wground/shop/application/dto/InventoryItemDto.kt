package kr.co.wground.shop.application.dto

import java.time.LocalDateTime
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.infra.dto.InventoryItemInfra

data class InventoryItemDto(
    val inventoryId: Long,
    val shopItemId: Long,
    val name: String,
    val description: String,
    val itemType: ShopItemType,
    val imageUrl: String,
    val consumable: Boolean,
    val equipped: Boolean,
    val remainingDays: Long?
) {
    companion object {
        fun from(
            infraDto: InventoryItemInfra,
            now: LocalDateTime = LocalDateTime.now(),
        ): InventoryItemDto {
            return InventoryItemDto(
                inventoryId = infraDto.inventoryId,
                shopItemId = infraDto.shopItemId,
                name = infraDto.name,
                description = infraDto.description,
                itemType = infraDto.itemType,
                imageUrl = infraDto.imageUrl,
                consumable = infraDto.consumable,
                equipped = infraDto.equipped,
                remainingDays = infraDto.remainDays(now)
            )
        }
    }
}