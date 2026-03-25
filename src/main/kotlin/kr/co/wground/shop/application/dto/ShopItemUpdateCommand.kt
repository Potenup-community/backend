package kr.co.wground.shop.application.dto

import org.springframework.web.multipart.MultipartFile

data class ShopItemUpdateCommand(
    val name: String?,
    val description: String?,
    val price: Long?,
    val file: MultipartFile?
)