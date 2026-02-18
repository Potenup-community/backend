package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.ShopItem
import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemSummaryDto(
    val id: Long,
    val name: String,
    val price: Long,
    val itemType: ShopItemType,
    val consumable: Boolean,
    val durationDays: Int?,
    val imageUrl: String,
) {
    companion object {
        fun from(item: ShopItem): ShopItemSummaryDto {
            return ShopItemSummaryDto(
                id = item.id,
                name = item.name,
                price = item.price,
                itemType = item.itemType,
                consumable = item.consumable,
                durationDays = item.durationDays,
                imageUrl = item.imageUrl,
            )
        }
    }
}