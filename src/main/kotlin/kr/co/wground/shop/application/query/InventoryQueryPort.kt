package kr.co.wground.shop.application.query

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.EquippedItemWithUserDto

interface InventoryQueryPort {
    fun getEquipItems(userIds: List<UserId>): List<EquippedItemWithUserDto>
}