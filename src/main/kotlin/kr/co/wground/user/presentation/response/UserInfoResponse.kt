package kr.co.wground.user.presentation.response

import kr.co.wground.like.domain.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole

data class UserInfoResponse(
    val userId: UserId,
    val email: String,
    val role: UserRole,
    val refreshToken: String?,
){
    //테스트용 정보 조회
    companion object{
        fun from(user: User): UserInfoResponse{
            return UserInfoResponse(
                userId = user.userId,
                email = user.email,
                role = user.role,
                refreshToken = user.refreshToken
            )
        }
    }
}
