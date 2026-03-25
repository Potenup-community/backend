package kr.co.wground.shop.infra.item

import kr.co.wground.shop.domain.ShopItem
import org.springframework.data.jpa.repository.JpaRepository

interface ShopItemRepository : JpaRepository<ShopItem, Long>, CustomShopItemRepository