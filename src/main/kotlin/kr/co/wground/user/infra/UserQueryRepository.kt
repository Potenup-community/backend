package kr.co.wground.user.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.infra.dto.UserCountDto
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.utils.email.event.VerificationEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserQueryRepository {
    fun searchUsers(condition: ConditionDto, pageable: Pageable): Page<UserInfoDto>
    fun findUserDisplayInfos(userIds: List<UserId>): Map<UserId, UserDisplayInfoDto>
    fun calculateCounts(conditionDto: ConditionDto): UserCountDto
    fun findAllApprovalTargets(userIds: List<Long>): List<VerificationEvent.VerificationTarget>
}
