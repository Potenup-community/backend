package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.constant.ShopItemType

data class EquippedItem(
    val itemType: ShopItemType,
    val imageUrl: String,
){
    companion object {
        fun from(equippedItemWithUserDto: EquippedItemWithUserDto): EquippedItem {
            return EquippedItem(
                itemType = equippedItemWithUserDto.itemType,
                imageUrl = equippedItemWithUserDto.imageUrl,
            )
        }
    }
}
