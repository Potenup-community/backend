package kr.co.wground.user.presentation.response

import kr.co.wground.global.common.UserId
import kr.co.wground.user.infra.dto.UserDisplayInfoDto

data class UserSummaryResponse(
    val userId: UserId,
    val name: String,
    val profileImageUrl: String,
    val trackName: String,
) {
    companion object {
        fun from(
            dto: UserDisplayInfoDto
        ): UserSummaryResponse {
            return UserSummaryResponse(
                userId = dto.userId,
                name = dto.name,
                profileImageUrl = dto.profileImageUrl,
                trackName = dto.trackName,
            )
        }
    }
}
