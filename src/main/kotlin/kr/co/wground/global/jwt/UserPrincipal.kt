package kr.co.wground.global.jwt

import kr.co.wground.global.common.UserId

data class UserPrincipal(
    val userId : UserId,
    val role : String
)
