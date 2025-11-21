package kr.co.wground.user.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.user.domain.constant.UserSignupStatus

@Entity
class RequestSignup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?= null,
    var affiliationId: Long,
    val email: String,
    val name: String,
    val role : String,
    val phoneNumber: String,
    val provider: String,
    @Enumerated(EnumType.STRING)
    val requestStatus: UserSignupStatus = UserSignupStatus.PENDING,
) : BaseEntity(){
    constructor(affiliationId : Long, email: String, name: String, role: String ,phoneNumber: String, provider: String) : this(
        id = null,
        affiliationId = affiliationId,
        email = email,
        name = name,
        role = role,
        phoneNumber = phoneNumber,
        provider = provider
    )

}
