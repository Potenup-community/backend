package kr.co.wground.shop.presentation.response

import kr.co.wground.shop.application.dto.InventoryItemDto
import kr.co.wground.shop.domain.constant.ShopItemType

data class InventoryItemGroupResponse(
    val itemType: ShopItemType,
    val items: List<InventoryItemDto>,
)