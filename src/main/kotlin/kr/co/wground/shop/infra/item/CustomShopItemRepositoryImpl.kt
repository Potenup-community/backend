package kr.co.wground.shop.infra.item

import com.querydsl.core.group.GroupBy.groupBy
import com.querydsl.core.group.GroupBy.list
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.shop.application.dto.ShopItemSummaryDto
import kr.co.wground.shop.domain.QShopItem.shopItem
import kr.co.wground.shop.domain.constant.ShopItemStatus
import org.springframework.stereotype.Repository

@Repository
class CustomShopItemRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomShopItemRepository {

    override fun findSummariesByStatus(status: ShopItemStatus): List<ShopItemSummaryDto> {
        return queryFactory
            .select(
                Projections.constructor(
                    ShopItemSummaryDto::class.java,
                    shopItem.id,
                    shopItem.name,
                    shopItem.description,
                    shopItem.price,
                    shopItem.itemType,
                    shopItem.consumable,
                    shopItem.durationDays,
                    shopItem.imageUrl,
                )
            )
            .from(shopItem)
            .where(shopItem.status.eq(status))
            .orderBy(
                shopItem.itemType.asc(),
                shopItem.createdAt.desc()
            )
            .fetch()
    }
}
