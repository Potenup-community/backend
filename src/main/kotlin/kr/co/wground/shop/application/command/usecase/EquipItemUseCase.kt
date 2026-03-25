package kr.co.wground.shop.application.command.usecase

import kr.co.wground.global.common.UserId

interface EquipItemUseCase {
    fun equip(userId: UserId, inventoryId: Long)
    fun unequip(userId: UserId, inventoryId: Long)
}