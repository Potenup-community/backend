package kr.co.wground.shop.infra.item

import kr.co.wground.shop.application.dto.ShopItemSummaryDto
import kr.co.wground.shop.domain.constant.ShopItemStatus

interface CustomShopItemRepository {
    fun findSummariesByStatus(status: ShopItemStatus): List<ShopItemSummaryDto>
}