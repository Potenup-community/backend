package kr.co.wground.user.presentation.response

import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.dto.MyInfoDto

data class ProfileResponse(
    val userId: UserId,
    val userName: String,
    val imageUrl: String,
){
    companion object {
        fun from(myInfoDto: MyInfoDto): ProfileResponse {
            return ProfileResponse(
                userId = myInfoDto.userId,
                userName = myInfoDto.name,
                imageUrl = myInfoDto.profileImageUrl
            )
        }
    }
}
