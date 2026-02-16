package kr.co.wground.shop.application.query

import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.application.dto.ShopItemSummaryDto

interface ShopQueryUseCase {
    fun getActiveItems(): List<ShopItemSummaryDto>
    fun getShopItemDetail(id: Long): ShopItemDetailDto
}