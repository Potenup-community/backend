package kr.co.wground.user.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.EquippedItemWithUserDto
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.infra.dto.MyPageDto
import kr.co.wground.user.infra.dto.UserCountDto
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.utils.email.event.VerificationEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomUserRepository {
    fun searchUsers(condition: ConditionDto, pageable: Pageable): Page<UserInfoDto>
    fun findUserDisplayInfos(userIds: List<UserId>): Map<UserId, UserDisplayInfoDto>
    fun findUserDisplayInfosForMention(
        size: Int,
        cursorId: Long?
    ): List<UserDisplayInfoDto>

    fun calculateCounts(conditionDto: ConditionDto): UserCountDto
    fun findUserAndTrack(userId: UserId): MyPageDto?
    fun findEquippedItemsByUserIds(userIds: List<UserId>): List<EquippedItemWithUserDto>
    fun findAllApprovalTargets(userIds: List<Long>): List<VerificationEvent.VerificationTarget>
}
