package kr.co.wground.shop.application.query

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.InventoryItemSummaryDto
import kr.co.wground.shop.presentation.response.InventoryItemGroupResponse

interface InventoryQueryUseCase {
    fun getMyInventory(userId: UserId): List<InventoryItemGroupResponse>
}