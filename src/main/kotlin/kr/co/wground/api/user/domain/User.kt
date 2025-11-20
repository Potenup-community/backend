package kr.co.wground.api.user.domain

import jakarta.persistence.*
import kr.co.wground.api.user.domain.constant.UserSignupStatus
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
    val status: UserSignupStatus = UserSignupStatus.PENDING,

    @Column(nullable = true)
    val phoneNumber: String?,

    @Column(nullable = true)
    val deletedAt: LocalDateTime? = null
) : BaseEntity() {

    constructor(
        affiliationId: Long,
        role: String,
        email: String,
        name: String,
        phoneNumber: String?
    ) : this(
        id = null, // 새로운 객체이므로 id는 null
        affiliationId = affiliationId,
        role = role,
        email = email,
        name = name,
        status = UserSignupStatus.PENDING, // 기본 상태
        phoneNumber = phoneNumber, // 기본 전화번호
        deletedAt = null // 기본 deletedAt
    )
}