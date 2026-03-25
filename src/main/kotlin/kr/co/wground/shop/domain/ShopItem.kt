package kr.co.wground.shop.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.co.wground.shop.domain.constant.ShopItemStatus
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.exception.ShopErrorCode
import kr.co.wground.exception.BusinessException
import java.time.LocalDateTime

@Entity
@Table(name = "shop_item")
class ShopItem private constructor(
    name: String,
    description: String,
    price: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val itemType: ShopItemType,

    @Column(nullable = false)
    val consumable: Boolean,

    val durationDays: Int?,

    imageUrl: String,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, length = 50)
    var name: String = name
        protected set

    @Column(nullable = false, length = 300)
    var description: String = description
        protected set

    @Column(nullable = false)
    var price: Long = price
        protected set

    @Column(nullable = false)
    var imageUrl: String = imageUrl
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ShopItemStatus = ShopItemStatus.ACTIVE
        protected set

    companion object {

        fun create(
            name: String,
            description: String,
            price: Long,
            itemType: ShopItemType,
            consumable: Boolean,
            durationDays: Int?,
            imageUrl: String,
        ): ShopItem {
            validateNotBlank(name, ShopErrorCode.INVALID_ITEM_NAME)
            validateNotBlank(description, ShopErrorCode.INVALID_ITEM_DESCRIPTION)
            validateNotBlank(imageUrl, ShopErrorCode.INVALID_ITEM_IMAGE_URL)
            validatePositivePrice(price)
            validateItemTypeWithDuration(consumable, durationDays)

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

        private fun validateNotBlank(value: String, errorCode: ShopErrorCode) {
            if (value.isBlank()) {
                throw BusinessException(errorCode)
            }
        }

        private fun validatePositivePrice(price: Long) {
            if (price <= 0) {
                throw BusinessException(ShopErrorCode.INVALID_PRICE_TO_CREATE)
            }
        }

        private fun validateItemTypeWithDuration(consumable: Boolean, durationDays: Int?) {
            if (consumable && (durationDays == null || durationDays <= 0)) {
                throw BusinessException(ShopErrorCode.CONSUMABLE_ITEM_NEED_DURATION)
            }
            if (!consumable && durationDays != null) {
                throw BusinessException(ShopErrorCode.PERMANENT_ITEM_SHOULD_NOT_HAVE_DURATION)
            }
        }
    }

    fun update(name: String, description: String, price: Long, imageUrl: String) {
        validateNotBlank(name, ShopErrorCode.INVALID_ITEM_NAME)
        validateNotBlank(description, ShopErrorCode.INVALID_ITEM_DESCRIPTION)
        validateNotBlank(imageUrl, ShopErrorCode.INVALID_ITEM_IMAGE_URL)
        validatePositivePrice(price)

        this.name = name
        this.description = description
        this.price = price
        this.imageUrl = imageUrl
    }


    fun hide() {
        status = ShopItemStatus.HIDDEN
    }

    fun activate() {
        status = ShopItemStatus.ACTIVE
    }

    fun getRequiredDurationDays(): Int {
        if (!this.consumable) {
            throw BusinessException(ShopErrorCode.PERMANENT_ITEM_SHOULD_NOT_HAVE_DURATION)
        }
        return this.durationDays
            ?: throw BusinessException(ShopErrorCode.CONSUMABLE_ITEM_NEED_DURATION)
    }

    fun validatePurchasable() {
        if (status != ShopItemStatus.ACTIVE) {
            throw BusinessException(ShopErrorCode.ITEM_NOT_AVAILABLE)
        }
    }
}