package kr.co.wground.shop.application.command

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.exception.PointErrorCode
import kr.co.wground.point.infra.wallet.PointWalletRepository
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
    private val walletRepository: PointWalletRepository
) : PurchaseItemUseCase {

    override fun purchase(userId: UserId, itemId: Long) {
        val wallet = walletRepository.findByIdOrNull(userId)
            ?: throw BusinessException(PointErrorCode.WALLET_NOT_FOUND)

        val shopItem = shopItemRepository.findByIdOrNull(itemId)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)

        shopItem.validatePurchasable()

        val existing = inventoryRepository.findByUserIdAndShopItemId(userId, shopItem.id)

        existing?.let {
            if(shopItem.consumable){
                throw BusinessException(ShopErrorCode.ALREADY_OWNED_ITEM)
            }
            validateRepurchasable(it, shopItem)
            wallet.deductBalance(shopItem.price)
            it.upgradeToPermanent(LocalDateTime.now())
        } ?: run {
            val inventory = createInventory(userId, shopItem)
            wallet.deductBalance(shopItem.price)
            inventoryRepository.save(inventory)
        }
    }


    private fun validateRepurchasable(existing: UserInventory, shopItem: ShopItem) {
        if (existing.isPermanent()) {
            throw BusinessException(ShopErrorCode.ALREADY_OWNED)
        }
        if (shopItem.consumable) {
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
