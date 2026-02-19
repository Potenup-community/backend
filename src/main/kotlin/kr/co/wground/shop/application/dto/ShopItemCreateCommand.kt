package kr.co.wground.shop.application.dto

import kr.co.wground.shop.domain.constant.ShopItemType
import org.springframework.web.multipart.MultipartFile

data class ShopItemCreateCommand(
    val name: String,
    val description: String,
    val price: Long,
    val itemType: ShopItemType,
    val consumable: Boolean,
    val durationDays: Int?,
    val file: MultipartFile,
)