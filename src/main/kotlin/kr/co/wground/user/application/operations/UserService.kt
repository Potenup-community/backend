package kr.co.wground.user.application.operations

import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.dto.MyInfoDto
import kr.co.wground.user.infra.dto.UserDisplayInfoDto

interface UserService {
    fun getMyInfo(userId: UserId): MyInfoDto
    fun getUsersForMention(limit: Int, cursorName: String?, cursorId: Long?): List<UserDisplayInfoDto>
}