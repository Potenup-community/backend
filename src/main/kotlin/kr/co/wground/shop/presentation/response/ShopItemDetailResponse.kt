package kr.co.wground.shop.presentation.response

import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemDetailResponse(
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
        fun from(dto: ShopItemDetailDto): ShopItemDetailResponse {
            return ShopItemDetailResponse(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                price = dto.price,
                itemType = dto.itemType,
                consumable = dto.consumable,
                durationDays = dto.durationDays,
                imageUrl = dto.imageUrl
            )
        }
    }
}
