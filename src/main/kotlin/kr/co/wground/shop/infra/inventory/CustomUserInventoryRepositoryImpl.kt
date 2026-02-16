package kr.co.wground.shop.infra.inventory

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.query.dto.InventoryItemDto
import kr.co.wground.shop.domain.QShopItem.shopItem
import kr.co.wground.shop.domain.QUserInventory.userInventory
import kr.co.wground.shop.domain.constant.ShopItemType
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import kr.co.wground.shop.application.dto.InventoryItemDto

@Repository
class CustomUserInventoryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomUserInventoryRepository {

    override fun findActiveWithItemByUserId(userId: UserId): List<InventoryItemDto> {
        val now = LocalDateTime.now()
        return queryFactory
            .select(
                Projections.constructor(
                    InventoryItemDto::class.java,
                    userInventory.id,
                    shopItem.id,
                    shopItem.name,
                    shopItem.description,
                    shopItem.itemType,
                    shopItem.imageUrl,
                    shopItem.consumable,
                    userInventory.equipped,
                    userInventory._expireAt,
                )
            )
            .from(userInventory)
            .join(shopItem).on(userInventory.shopItemId.eq(shopItem.id))
            .where(
                userInventory.userId.eq(userId),
                userInventory._expireAt.isNull
                    .or(userInventory._expireAt.after(now))
            )
            .orderBy(userInventory.itemType.asc(), userInventory.acquiredAt.desc())
            .fetch()
    }
}