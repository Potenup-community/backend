package kr.co.wground.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.user.controller.dto.request.SignUpRequest
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class RequestSignup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_signup_id", nullable = false)
    val requestSignupId: Long?,

    @Column(nullable = false)
    var affiliationId: Long,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val provider: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var deletedAt: LocalDateTime? = null,

    role: UserRole = UserRole.BLOCKED,

    status: UserSignupStatus = UserSignupStatus.PENDING,

    ) {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = role
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var requestStatus: UserSignupStatus = status
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun approve() {
        this.requestStatus = UserSignupStatus.ACCEPTED
    }

    fun reject() {
        this.requestStatus = UserSignupStatus.REJECTED
    }
}
