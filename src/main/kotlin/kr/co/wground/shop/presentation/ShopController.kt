package kr.co.wground.shop.presentation

import kotlin.collections.orEmpty
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.shop.application.command.usecase.PurchaseItemUseCase
import kr.co.wground.shop.application.query.ShopQueryUseCase
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.presentation.response.ShopItemDetailResponse
import kr.co.wground.shop.presentation.response.ShopItemGroupResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/shop")
class ShopController(
    private val purchaseItemUseCase: PurchaseItemUseCase,
    private val queryShopUseCase: ShopQueryUseCase,
): ShopApi {

    @GetMapping("/items")
    override fun getShopItems(): ResponseEntity<List<ShopItemGroupResponse>> {
        val items = queryShopUseCase.getActiveItems()
        val grouped = items.groupBy { it.itemType }

        val response = ShopItemType.entries.map { type ->
            ShopItemGroupResponse(
                itemType = type,
                items = grouped[type].orEmpty()
            )
        }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/items/{itemId}")
    override fun getShopItemDetail(@PathVariable itemId: Long): ResponseEntity<ShopItemDetailResponse> {
        val item = queryShopUseCase.getShopItemDetail(itemId)
        return ResponseEntity.ok(ShopItemDetailResponse.from(item))
    }

    @PostMapping("/items/{itemId}/purchase")
    override fun purchaseItem(
        userId: CurrentUserId,
        @PathVariable itemId: Long,
    ): ResponseEntity<Unit> {
        purchaseItemUseCase.purchase(userId.value, itemId)
        return ResponseEntity.noContent().build()
    }
}