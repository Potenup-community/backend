package kr.co.wground.shop.application.query

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.infra.inventory.UserInventoryRepository
import kr.co.wground.shop.presentation.response.InventoryItemGroupResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InventoryQueryService(
    private val inventoryRepository: UserInventoryRepository
) : InventoryQueryUseCase {
    override fun getMyInventory(userId: UserId): List<InventoryItemGroupResponse> {
        val now = LocalDateTime.now()
        val items = inventoryRepository.findActiveItemByUserId(userId, now)

        val grouped = items.groupBy { it.itemType }

        return ShopItemType.entries.map { type ->
            InventoryItemGroupResponse(
                itemType = type,
                items = grouped[type].orEmpty()
            )
        }
    }
}