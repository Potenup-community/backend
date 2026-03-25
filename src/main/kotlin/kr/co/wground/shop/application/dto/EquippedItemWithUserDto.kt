package kr.co.wground.shop.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.domain.constant.ShopItemType

data class EquippedItemWithUserDto(
    val userId: UserId,
    val itemType: ShopItemType,
    val imageUrl: String,
)
