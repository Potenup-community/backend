package kr.co.wground.point.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.exception.PointErrorCode
import java.time.LocalDateTime

@Entity
@Table(name = "point_wallet")
class PointWallet private constructor(
    @Id
    @Column(name = "user_id")
    val userId: UserId
) {
    @Column(nullable = false)
    var balance: Long = 0L
        protected set

    @Column(name = "last_updated_at", nullable = false)
    var lastUpdatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Version
    var version: Long = 0L
        protected set

    fun addBalance(amount: Long) {
        validatePositiveAmount(amount)

        applyBalanceChange(amount)
    }

    fun deductBalance(amount: Long) {
        validatePositiveAmount(amount)
        validateSufficientBalance(amount)

        applyBalanceChange(-amount)
    }

    private fun applyBalanceChange(amount: Long) {
        this.balance += amount
        this.lastUpdatedAt = LocalDateTime.now()
    }

    private fun validatePositiveAmount(amount: Long) {
        if (isPositive(amount)) {
            throw BusinessException(PointErrorCode.INVALID_AMOUNT)
        }
    }

    private fun validateSufficientBalance(amount: Long) {
        if (isBalanceNotSufficient(amount)) {
            throw BusinessException(PointErrorCode.NOT_SUFFICIENT_BALANCE)
        }
    }

    private fun isPositive(amount: Long): Boolean {
        return amount <= 0
    }

    private fun isBalanceNotSufficient(amount: Long): Boolean {
        return this.balance < amount
    }

    companion object {
        fun create(userId: UserId): PointWallet {
            validateUserId(userId)
            return PointWallet(userId)
        }

        private fun validateUserId(userId: UserId) {
            if (isInvalidUserId(userId)) {
                throw BusinessException(PointErrorCode.INVALID_USER_ID)
            }
        }

        private fun isInvalidUserId(userId: UserId): Boolean {
            return userId <= 0
        }
    }
}