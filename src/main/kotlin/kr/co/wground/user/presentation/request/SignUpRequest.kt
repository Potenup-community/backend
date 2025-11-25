package kr.co.wground.user.presentation.request

data class SignUpRequest(
    val idToken: String,
    val trackId: Long,
    val name : String,
    val phoneNumber: String,
    val provider: String,
)
