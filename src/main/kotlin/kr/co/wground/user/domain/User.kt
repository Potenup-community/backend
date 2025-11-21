package kr.co.wground.user.domain

import jakarta.persistence.*
import kr.co.wground.user.domain.constant.UserStatus
import java.time.LocalDateTime

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "affiliation_id", nullable = false)
    val affiliationId: Long,

    @Column(nullable = false)
    val role: String,

    @Column(unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: UserStatus,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val provider: String,

    @Column(nullable = true)
    val deletedAt: LocalDateTime? = null
) : BaseEntity() {

    constructor(
        affiliationId: Long,
        role: String,
        email: String,
        name: String,
        status: UserStatus,
        phoneNumber: String,
        provider: String
    ) : this(
        id = null,
        affiliationId = affiliationId,
        role = role,
        email = email,
        name = name,
        status = status,
        phoneNumber = phoneNumber,
        deletedAt = null,
        provider = provider
    )

    fun decisionStatus(role : String) = UserStatus.from(role)
}