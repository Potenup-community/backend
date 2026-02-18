package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.ShopItem
import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemDetailDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Long,
    val itemType: ShopItemType,
    val consumable: Boolean,
    val durationDays: Int?,
    val imageUrl: String,
){
    companion object {
        fun from(shopItem: ShopItem): ShopItemDetailDto {
            return ShopItemDetailDto(
                id = shopItem.id,
                name = shopItem.name,
                description = shopItem.description,
                price = shopItem.price,
                itemType = shopItem.itemType,
                consumable = shopItem.consumable,
                durationDays = shopItem.durationDays,
                imageUrl = shopItem.imageUrl
            )
        }
    }
}
