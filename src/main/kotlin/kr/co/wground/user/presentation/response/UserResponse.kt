package kr.co.wground.user.presentation.response

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.dto.MyInfoDto
import kr.co.wground.user.domain.constant.UserRole

data class UserResponse(
    val userId: UserId,
    val name: String,
    val email: String,
    val trackId: TrackId,
    val trackName: String,
    val profileImageUrl: String,
    val role: UserRole,
) {
    companion object {
        fun from(myInfo: MyInfoDto): UserResponse {
            return UserResponse(
                userId = myInfo.userId,
                name = myInfo.name,
                email = myInfo.email,
                trackId = myInfo.trackId,
                trackName = myInfo.trackName,
                profileImageUrl = myInfo.profileImageUrl,
                role = myInfo.role,
            )
        }
    }
}
