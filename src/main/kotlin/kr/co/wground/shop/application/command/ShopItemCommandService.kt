package kr.co.wground.shop.application.command

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.application.command.usecase.PurchasePointUseCase
import kr.co.wground.shop.application.command.usecase.AdminShopItemUseCase
import kr.co.wground.shop.application.command.usecase.PurchaseItemUseCase
import kr.co.wground.shop.application.dto.ShopItemCreateCommand
import kr.co.wground.shop.application.dto.ShopItemDetailDto
import kr.co.wground.shop.application.dto.ShopItemUpdateCommand
import kr.co.wground.shop.domain.ShopItem
import kr.co.wground.shop.domain.UserInventory
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.shop.infra.image.ItemImageStore
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
    private val itemImageStore: ItemImageStore,
    private val purchasePointUseCase: PurchasePointUseCase
) : PurchaseItemUseCase, AdminShopItemUseCase {

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

    override fun createItem(command: ShopItemCreateCommand): ShopItemDetailDto {
        val imageUrl = itemImageStore.store(command.file)

        val shopItem = ShopItem.create(
            name = command.name,
            description = command.description,
            price = command.price,
            itemType = command.itemType,
            consumable = command.consumable,
            durationDays = command.durationDays,
            imageUrl = imageUrl,
        )

        shopItemRepository.save(shopItem)
        return ShopItemDetailDto.from(shopItem)
    }

    override fun updateItem(
        itemId: Long,
        command: ShopItemUpdateCommand
    ): ShopItemDetailDto {

        val shopItem = shopItemRepository.findByIdOrNull(itemId)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)

        val resolved = resolveFields(command, shopItem)

        shopItem.update(
            name = resolved.name,
            description = resolved.description,
            price = resolved.price,
            imageUrl = resolved.imageUrl
        )

        return ShopItemDetailDto.from(shopItem)
    }

    override fun hide(itemId: Long) {
        val shopItem = shopItemRepository.findByIdOrNull(itemId)
            ?: throw BusinessException(ShopErrorCode.ITEM_NOT_FOUND)

        shopItem.hide()
    }

    private fun resolveFields(
        command: ShopItemUpdateCommand,
        shopItem: ShopItem
    ): ResolvedShopItemUpdate {

        val name = command.name ?: shopItem.name
        val description = command.description ?: shopItem.description
        val price = command.price ?: shopItem.price

        return ResolvedShopItemUpdate(
            name = name,
            description = description,
            price = price,
            imageUrl = resolveImageUrl(command, shopItem)
        )
    }

    private fun resolveImageUrl(
        command: ShopItemUpdateCommand,
        shopItem: ShopItem
    ): String {

        return command.file?.let { file ->
            val newImageUrl = itemImageStore.store(file)
            itemImageStore.delete(shopItem.imageUrl)
            newImageUrl
        } ?: shopItem.imageUrl
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

    private data class ResolvedShopItemUpdate(
        val name: String,
        val description: String,
        val price: Long,
        val imageUrl: String,
    )
}
