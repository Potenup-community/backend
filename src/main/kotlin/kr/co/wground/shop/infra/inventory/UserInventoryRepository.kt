package kr.co.wground.shop.infra.inventory

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.domain.UserInventory
import org.springframework.data.jpa.repository.JpaRepository

interface UserInventoryRepository : JpaRepository<UserInventory, Long>, CustomUserInventoryRepository {
    fun findByUserIdAndShopItemId(userId: UserId, shopItemId: Long): UserInventory?
}