package kr.co.wground.api.user.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import kr.co.wground.api.user.domain.constant.UserSignupStatus

@Entity
class RequestSignup(
    @Id
    val id: Long?= null,
    val affiliationId: Long,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val provider: String,
    val requestStatus: UserSignupStatus = UserSignupStatus.PENDING,
) : BaseEntity(){
    constructor(affiliationId : Long, email: String, name: String, phoneNumber: String, provider: String) : this(
        id = null,
        affiliationId = affiliationId,
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        provider = provider
    )
}
