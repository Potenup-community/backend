package kr.co.wground.shop.application.query

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.InventoryItemDto
import kr.co.wground.shop.infra.inventory.UserInventoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InventoryQueryService(
    private val inventoryRepository: UserInventoryRepository
) : InventoryQueryUseCase {

    override fun getMyInventory(userId: UserId): List<InventoryItemDto> {
        val now = LocalDateTime.now()
        return inventoryRepository.findActiveItemByUserId(userId, now)
    }
}