package kr.co.wground.user.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.infra.dto.UserInfoDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomUserRepository {
    fun searchUsers(condition: ConditionDto, pageable: Pageable): Page<UserInfoDto>
    fun findUserAndTrackName(userId: List<UserId>) : Map<Long, String?>
}
