package kr.co.wground.shop.presentation.request

import jakarta.validation.constraints.Min
import kr.co.wground.shop.application.dto.ShopItemCreateCommand
import kr.co.wground.shop.domain.constant.ShopItemType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

data class AdminShopItemCreateRequest(
    @field:NotBlank(message = "아이템 이름은 빈 값일 수 없습니다.")
    val name: String,
    @field:NotBlank(message = "아이템 설명은 빈 값일 수 없습니다.")
    val description: String,
    @field:Min(1, message = "가격은 0보다 커야 합니다.")
    val price: Long,
    @field:NotNull(message = "아이템 타입은 빈 값일 수 없습니다.")
    val itemType: ShopItemType,
    @field:NotNull(message = "아이템 유지 타입은 빈 값일 수 없습니다.")
    val consumable: Boolean,
    val durationDays: Int?,
    val file: MultipartFile,
) {
    fun toCommand(): ShopItemCreateCommand {
        return ShopItemCreateCommand(
            name = name,
            description = description,
            price = price,
            itemType = itemType,
            consumable = consumable,
            durationDays = durationDays,
            file = file
        )
    }
}
