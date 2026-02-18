package kr.co.wground.shop.infra.inventory

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.InventoryItemDto

interface CustomUserInventoryRepository {
    fun findActiveItemByUserId(userId: UserId, now: LocalDateTime): List<InventoryItemDto>
}