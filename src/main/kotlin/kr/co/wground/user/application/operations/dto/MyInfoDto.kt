package kr.co.wground.user.application.operations.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole

data class MyInfoDto(
    val userId: UserId,
    val name: String,
    val email: String,
    val trackId: TrackId,
    val profileImageUrl: String,
    val role: UserRole,
){
    companion object{
        fun from(user: User): MyInfoDto{
            return MyInfoDto(
                userId = user.userId,
                name = user.name,
                email = user.email,
                trackId = user.trackId,
                profileImageUrl = user.userProfile.profileImageUrl,
                role = user.role,
            )
        }
    }
}
