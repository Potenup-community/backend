package kr.co.wground.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.user.domain.constant.UserSignupStatus
import java.time.LocalDateTime

@Entity
class RequestSignup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_signup_id")
    val requestSignupId: Long?,

    val userId: Long,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var deletedAt: LocalDateTime? = null,

    status: UserSignupStatus = UserSignupStatus.PENDING,

    ) {
    @Enumerated(EnumType.STRING)
    var requestStatus: UserSignupStatus = status
        protected set

    var modifiedAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun approve() {
        this.requestStatus = UserSignupStatus.ACCEPTED
    }

    fun reject() {
        this.requestStatus = UserSignupStatus.REJECTED
    }
}
