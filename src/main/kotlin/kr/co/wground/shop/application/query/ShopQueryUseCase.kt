package kr.co.wground.shop.application.query

import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.presentation.response.ShopItemGroupResponse

interface ShopQueryUseCase {
    fun getActiveItems(): List<ShopItemGroupResponse>
    fun getShopItemDetail(id: Long): ShopItemDetailDto
}