package kr.co.wground.user.application.operations.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.infra.dto.MyPageDto

data class MyInfoDto(
    val userId: UserId,
    val name: String,
    val email: String,
    val trackId: TrackId,
    val trackName: String,
    val profileImageUrl: String,
    val role: UserRole,
){
    companion object{
        fun from(user: MyPageDto): MyInfoDto{
            return MyInfoDto(
                userId = user.userId,
                name = user.name,
                email = user.email,
                trackId = user.trackId,
                profileImageUrl = user.accessProfile(),
                role = user.role,
            )
        }
    }
}
