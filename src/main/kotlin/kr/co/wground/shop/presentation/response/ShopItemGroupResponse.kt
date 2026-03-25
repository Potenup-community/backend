package kr.co.wground.shop.presentation.response

import kr.co.wground.shop.application.dto.ShopItemSummaryDto
import kr.co.wground.shop.domain.constant.ShopItemType

data class ShopItemGroupResponse(
    val itemType: ShopItemType,
    val items: List<ShopItemSummaryDto>,
)