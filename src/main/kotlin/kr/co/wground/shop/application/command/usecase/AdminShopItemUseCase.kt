package kr.co.wground.shop.application.command.usecase

import kr.co.wground.shop.application.dto.ShopItemCreateCommand
import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.application.dto.ShopItemUpdateCommand

interface AdminShopItemUseCase {
    fun createItem(command: ShopItemCreateCommand): ShopItemDetailDto
    fun updateItem(itemId: Long, command: ShopItemUpdateCommand): ShopItemDetailDto
    fun hide(itemId: Long)
}