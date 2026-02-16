package kr.co.wground.shop.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import kr.co.wground.shop.domain.constant.ShopItemStatus
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.exception.BusinessException
import java.time.LocalDateTime

@Entity
@Table(name = "shop_item")
class ShopItem private constructor(
    @Column(nullable = false, length = 50)
    val name: String,

    @Column(nullable = false, length = 300)
    val description: String,

    @Column(nullable = false)
    val price: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val itemType: ShopItemType,

    @Column(nullable = false)
    val consumable: Boolean,

    val durationDays: Int?,

    val imageUrl: String?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ShopItemStatus = ShopItemStatus.ACTIVE
        protected set

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun validatePurchasable() {
        if (status != ShopItemStatus.ACTIVE) {
            throw BusinessException(ShopErrorCode.ITEM_NOT_AVAILABLE)
        }
    }

    companion object {
        fun create(
            name: String,
            description: String,
            price: Long,
            itemType: ShopItemType,
            consumable: Boolean,
            durationDays: Int?,
            imageUrl: String?,
        ): ShopItem {
            if(price <= 0) {
                throw BusinessException(ShopErrorCode.INVALID_PRICE_TO_CREATE)
            }
            if (consumable && durationDays == null) {
                throw BusinessException(ShopErrorCode.CONSUMABLE_ITEM_NEED_DURATION)
            }

            return ShopItem(
                name = name,
                description = description,
                price = price,
                itemType = itemType,
                consumable = consumable,
                durationDays = durationDays,
                imageUrl = imageUrl,
            )
        }
    }
}