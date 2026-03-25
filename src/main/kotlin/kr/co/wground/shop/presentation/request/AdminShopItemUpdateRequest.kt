package kr.co.wground.shop.presentation.request

import kr.co.wground.shop.application.dto.ShopItemUpdateCommand
import org.springframework.web.multipart.MultipartFile

data class AdminShopItemUpdateRequest(
    val name: String?,
    val description: String?,
    val price: Long?,
    val file: MultipartFile?,
) {
    fun toCommand(): ShopItemUpdateCommand {
        return ShopItemUpdateCommand(
            name = name,
            description = description,
            price = price,
            file = file
        )
    }
}