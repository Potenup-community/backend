package kr.co.wground.shop.presentation

import kotlin.collections.orEmpty
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.shop.application.command.usecase.EquipItemUseCase
import kr.co.wground.shop.application.query.InventoryQueryUseCase
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.presentation.response.InventoryItemGroupResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/inventory")
class InventoryController(
    private val inventoryQueryUseCase: InventoryQueryUseCase,
    private val equipItemUseCase: EquipItemUseCase
) {

    @GetMapping("/me")
    fun getMyInventory(userId: CurrentUserId): ResponseEntity<List<InventoryItemGroupResponse>> {
        val inventory = inventoryQueryUseCase.getMyInventory(userId.value)
        val grouped = inventory.groupBy { it.itemType }

        val response = ShopItemType.entries.map { type ->
            InventoryItemGroupResponse(
                itemType = type,
                items = grouped[type].orEmpty()
            )
        }
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{inventoryId}/equip")
    fun equipItem(
        userId: CurrentUserId,
        @PathVariable inventoryId: Long,
    ): ResponseEntity<Unit> {
        equipItemUseCase.equip(userId.value, inventoryId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{inventoryId}/unequip")
    fun unequipItem(
        userId: CurrentUserId,
        @PathVariable inventoryId: Long,
    ): ResponseEntity<Unit> {
        equipItemUseCase.unequip(userId.value, inventoryId)
        return ResponseEntity.noContent().build()
    }
}