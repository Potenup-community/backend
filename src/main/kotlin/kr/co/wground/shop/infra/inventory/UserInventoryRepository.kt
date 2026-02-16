package kr.co.wground.shop.infra.inventory

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.domain.UserInventory
import kr.co.wground.shop.domain.constant.ShopItemType
import org.springframework.data.jpa.repository.JpaRepository

interface UserInventoryRepository : JpaRepository<UserInventory, Long>, CustomUserInventoryRepository {
    fun findByUserIdAndShopItemId(userId: UserId, shopItemId: Long): UserInventory?
    fun findAllEquippedByUserIdAndItemType(userId: UserId, itemType: ShopItemType) : List<UserInventory>
}