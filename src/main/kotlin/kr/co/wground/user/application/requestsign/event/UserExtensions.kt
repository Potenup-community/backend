import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

fun SignUpRequest.toUserEntity(email: String): User{
    return User(
        userId = null,
        affiliationId = this.affiliationId,
        email = email,
        name = this.name,
        phoneNumber = this.phoneNumber,
        provider = this.provider,
        role = this.role,
    )
}

fun User.toRequestSignup() : RequestSignup {
    return RequestSignup(
        requestSignupId = null,
        userId = this.userId!!
    )
}