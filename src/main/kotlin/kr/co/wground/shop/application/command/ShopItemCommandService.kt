package kr.co.wground.shop.application.command

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.command.usecase.PurchasePointUseCase
import kr.co.wground.shop.application.command.usecase.PurchaseItemUseCase
import kr.co.wground.shop.domain.ShopItem
import kr.co.wground.shop.domain.UserInventory
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.shop.infra.inventory.UserInventoryRepository
import kr.co.wground.shop.infra.item.ShopItemRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ShopItemCommandService(
    private val shopItemRepository: ShopItemRepository,
    private val inventoryRepository: UserInventoryRepository,
    private val purchasePointUseCase: PurchasePointUseCase
) : PurchaseItemUseCase {

    override fun purchase(userId: UserId, itemId: Long) {
        val shopItem = shopItemRepository.findByIdOrNull(itemId)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)

        shopItem.validatePurchasable()

        val existing = inventoryRepository.findByUserIdAndShopItemId(userId, shopItem.id)

        existing?.let {
            validateRepurchasable(it)
            purchasePointUseCase.forUpgradePurchase(userId, shopItem.price, shopItem.id)
            it.upgradeToPermanent(LocalDateTime.now())
        } ?: run {
            val inventory = createInventory(userId, shopItem)
            purchasePointUseCase.forPurchase(userId, shopItem.price, shopItem.id)
            inventoryRepository.save(inventory)
        }
    }


    private fun validateRepurchasable(existing: UserInventory) {
        if (existing.isPermanent()) {
            throw BusinessException(ShopErrorCode.ALREADY_OWNED)
        }
    }

    private fun createInventory(userId: UserId, shopItem: ShopItem): UserInventory {
        return if (shopItem.consumable) {
            UserInventory.createConsumable(
                userId = userId,
                shopItemId = shopItem.id,
                itemType = shopItem.itemType,
                durationDays = shopItem.getRequiredDurationDays(),
            )
        } else {
            UserInventory.createPermanent(
                userId = userId,
                shopItemId = shopItem.id,
                itemType = shopItem.itemType,
            )
        }
    }
}
