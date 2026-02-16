package kr.co.wground.shop.application.command

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.command.usecase.EquipItemUseCase
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.shop.infra.inventory.UserInventoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InventoryCommandService(
    private val inventoryRepository: UserInventoryRepository
): EquipItemUseCase {

    override fun equip(userId: UserId, inventoryId: Long) {
        val now = LocalDateTime.now()
        val inventory = inventoryRepository.findByIdOrNull(inventoryId)
            ?: throw BusinessException(ShopErrorCode.INVENTORY_NOT_FOUND)

        inventory.validateOwner(userId)
        val equippedItems = inventoryRepository
            .findAllEquippedByUserIdAndItemType(userId, inventory.itemType)

        equippedItems
            .filter { it.id != inventory.id }
            .forEach { it.unequip() }

        inventory.equip(now)
    }

    override fun unequip(userId: UserId, inventoryId: Long) {
        val inventory = inventoryRepository.findByIdOrNull(inventoryId)
            ?: throw BusinessException(ShopErrorCode.INVENTORY_NOT_FOUND)

        inventory.validateOwner(userId)
        inventory.unequip()
    }
}