package kr.co.wground.api.user.domain

import jakarta.persistence.*
import kr.co.wground.api.user.domain.constant.UserSignupStatus
import kr.co.wground.api.user.domain.constant.UserStatus
import java.time.LocalDateTime

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "affiliation_id", nullable = false)
    val affiliationId: Long,

    @Column(nullable = false)
    val role: String,

    @Column(unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val status: UserStatus,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = true)
    val deletedAt: LocalDateTime? = null
) : BaseEntity() {

    constructor(
        affiliationId: Long,
        role: String,
        email: String,
        name: String,
        status: UserStatus,
        phoneNumber: String
    ) : this(
        id = null,
        affiliationId = affiliationId,
        role = role,
        email = email,
        name = name,
        status = status,
        phoneNumber = phoneNumber,
        deletedAt = null
    )

    fun decisionStatus(role : String) = UserStatus.from(role)
}