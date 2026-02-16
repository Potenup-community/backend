package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemSummaryDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Long,
    val itemType: ShopItemType,
    val consumable: Boolean,
    val durationDays: Int?,
    val imageUrl: String,
) {

}