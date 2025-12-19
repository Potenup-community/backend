package kr.co.wground.user.application.operations

import kr.co.wground.global.common.UserId
import kr.co.wground.user.presentation.response.UserResponse

interface UserService {
    fun getMyInfo(userId: UserId): UserResponse
}