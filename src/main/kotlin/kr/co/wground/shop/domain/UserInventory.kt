package kr.co.wground.shop.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.domain.constant.ShopItemType
import kr.co.wground.shop.exception.ShopErrorCode
import java.time.Duration
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_inventory",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_inventory",
            columnNames = ["user_id", "shop_item_id"]
        )
    ]
)
class UserInventory private constructor(
    @Column(nullable = false, updatable = false)
    val userId: UserId,

    @Column(nullable = false, updatable = false)
    val shopItemId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val itemType: ShopItemType,

    expireAt: LocalDateTime?
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    var equipped: Boolean = false
        protected set

    @Column(name = "expire_at")
    protected var _expireAt: LocalDateTime? = expireAt
        protected set

    @Column(nullable = false, updatable = false)
    var acquiredAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Version
    var version: Long = 0L
        protected set

    companion object {

        fun createPermanent(
            userId: UserId,
            shopItemId: Long,
            itemType: ShopItemType
        ): UserInventory {
            return UserInventory(
                userId = userId,
                shopItemId = shopItemId,
                itemType = itemType,
                expireAt = null
            )
        }

        fun createConsumable(
            userId: UserId,
            shopItemId: Long,
            itemType: ShopItemType,
            durationDays: Int
        ): UserInventory {

            validateDuration(durationDays)

            return UserInventory(
                userId = userId,
                shopItemId = shopItemId,
                itemType = itemType,
                expireAt = LocalDateTime.now().plusDays(durationDays.toLong())
            )
        }

        private fun validateDuration(durationDays: Int) {
            if (durationDays <= 0) {
                throw BusinessException(ShopErrorCode.DURATION_DAYS_MUST_POSITIVE)
            }
        }
    }

    fun isPermanent(): Boolean {
        return _expireAt == null
    }

    fun isActive(now: LocalDateTime): Boolean {
        return _expireAt?.let { now.isBefore(it) } ?: true
    }


    fun remainDays(now: LocalDateTime): Long? {
        return _expireAt
            ?.let { Duration.between(now, it).toDays() }
            ?.coerceAtLeast(0)
    }

    fun refreshEquippedState(now: LocalDateTime) {
        if (!isActive(now)) {
            this.equipped = false
        }
    }

    fun equip(now: LocalDateTime) {
        if (!isActive(now)) {
            this.equipped = false
            throw BusinessException(ShopErrorCode.ITEM_EXPIRED)
        }

        this.equipped = true
    }

    fun unequip() {
        this.equipped = false
    }

    fun upgradeToPermanent(now: LocalDateTime) {
        refreshEquippedState(now)

        if (isPermanent()) {
            throw BusinessException(ShopErrorCode.ALREADY_OWNED_ITEM)
        }

        this._expireAt = null
    }

    fun validateOwner(userId: UserId) {
        if (this.userId != userId) {
            throw BusinessException(ShopErrorCode.NOT_OWNER)
        }
    }
}
