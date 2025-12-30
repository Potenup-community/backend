package kr.co.wground.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserSignupStatus
import java.time.LocalDateTime

@Entity
class RequestSignup(
    val userId: UserId,
    status: UserSignupStatus = UserSignupStatus.PENDING,
    ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val requestSignupId: Long = 0

    @Enumerated(EnumType.STRING)
    var requestStatus: UserSignupStatus = status
        protected set

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    var deletedAt: LocalDateTime? = null
    var modifiedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @PreUpdate
    fun onPreUpdate() {
        modifiedAt = LocalDateTime.now()
    }

    fun decide(status: UserSignupStatus) {
        validateUserStatus(status)
        requestStatus = status
    }

    private fun validateUserStatus(targetStatus: UserSignupStatus) {
        val isValid = when (requestStatus) {
            UserSignupStatus.PENDING ->
                targetStatus == UserSignupStatus.ACCEPTED || targetStatus == UserSignupStatus.REJECTED
            UserSignupStatus.ACCEPTED ->
                targetStatus == UserSignupStatus.REJECTED || targetStatus == UserSignupStatus.ACCEPTED
            UserSignupStatus.REJECTED ->
                targetStatus == UserSignupStatus.ACCEPTED || targetStatus == UserSignupStatus.REJECTED
            else -> false
        }

        if (!isValid) {
            throw BusinessException(UserServiceErrorCode.INVALID_STATUS_TRANSITION)
        }
    }
}
