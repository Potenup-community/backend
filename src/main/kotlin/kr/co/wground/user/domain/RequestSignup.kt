package kr.co.wground.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import kr.co.wground.user.domain.constant.UserSignupStatus
import java.time.LocalDateTime

@Entity
class RequestSignup(
    val userId: Long,
    status: UserSignupStatus = UserSignupStatus.PENDING,
    ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val requestSignupId: Long? = null

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
    fun approve() {
        this.requestStatus = UserSignupStatus.ACCEPTED
    }

    fun reject() {
        this.requestStatus = UserSignupStatus.REJECTED
    }
}
