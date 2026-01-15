package kr.co.wground.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import kr.co.wground.user.domain.vo.RefreshToken
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH

@Entity
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val userId: UserId = 0,

    @Column(nullable = false)
    val trackId: TrackId,

    @Column(unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val provider: String,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val deletedAt: LocalDateTime? = null,

    role: UserRole = UserRole.MEMBER,

    status: UserStatus = UserStatus.BLOCKED

) {
    @Embedded
    var userProfile: UserProfile = UserProfile.default()
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = role
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = status
        protected set

    @Column
    var modifiedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Embedded
    var refreshToken: RefreshToken = RefreshToken()
        get() = field ?: RefreshToken()
        protected set

    fun updateRefreshToken(newHashedToken: String) {
        this.refreshToken = this.refreshToken.rotate(newHashedToken)
    }

    fun validateRefreshToken(hashedToken: String): Boolean {
        return this.refreshToken.isValid(hashedToken)
    }

    @PrePersist
    fun initRefreshToken() {
        if (this.refreshToken == null) this.refreshToken = RefreshToken()
    }

    @PreUpdate
    fun onPreUpdate() {
        modifiedAt = LocalDateTime.now()
    }

    fun updateUserProfile(userProfile: UserProfile) {
        this.userProfile = userProfile
    }

    fun accessProfile(): String{
        return this.userProfile.imageUrl
    }

    fun fixAccessProfile() {
        this.userProfile.imageUrl = DEFAULT_AVATAR_PATH + "/${this.userId}"
    }

    fun logout() {
        this.refreshToken = RefreshToken()
    }

    fun toAdmin() {
        this.role = UserRole.ADMIN
    }

    fun toInstructor() {
        this.role = UserRole.INSTRUCTOR
    }

    fun toMember() {
        this.role = UserRole.MEMBER
    }

    fun decide(status: UserSignupStatus, role: UserRole?) {
        when (status) {
            UserSignupStatus.ACCEPTED -> {
                val decidedRole = role ?: throw BusinessException(UserServiceErrorCode.APPROVE_NECESSARY_ROLE)
                this.status = UserStatus.ACTIVE
                this.role = decidedRole
            }

            UserSignupStatus.REJECTED,
            UserSignupStatus.PENDING -> this.status = UserStatus.BLOCKED
        }
    }
}
