package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemGroupDto(
    val itemType: ShopItemType,
    val items: List<ShopItemSummaryDto>,
)
