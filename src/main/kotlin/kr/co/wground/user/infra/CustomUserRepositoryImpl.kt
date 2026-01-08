package kr.co.wground.user.infra

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.QTrack.track
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.domain.QRequestSignup.requestSignup
import kr.co.wground.user.domain.QUser.user
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.dto.UserInfoDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class CustomUserRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomUserRepository {
    override fun searchUsers(
        condition: ConditionDto,
        pageable: Pageable
    ): Page<UserInfoDto> {
        val predicatesArray = predicates(condition)

        val content = queryFactory
            .select(
                Projections.constructor(
                    UserInfoDto::class.java,
                    user.userId,
                    user.name,
                    user.email,
                    user.phoneNumber,
                    user.trackId,
                    user.role,
                    user.status,
                    requestSignup.requestStatus,
                    user.provider,
                    user.createdAt,
                    user.modifiedAt
                )
            )
            .from(user)
            .leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId))
            .where(
                *predicatesArray
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(user.createdAt.desc())
            .fetch()

        val countQuery = getUserCountQuery(condition, predicatesArray)

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun getUserCountQuery(
        condition: ConditionDto,
        predicates: Array<BooleanExpression?>
    ): JPAQuery<Long> {

        val countQuery = queryFactory
            .select(user.count())
            .from(user)

        if (condition.requestStatus != null) {
            countQuery.leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId))
        }

        countQuery.where(*predicates)

        return countQuery
    }

    override fun findUserAndTrackName(
        userId: UserId
    ): Pair<String?, String?> {
        val result = queryFactory
            .select(
                user.name,
                track.trackName
            )
            .from(user)
            .leftJoin(track).on(user.trackId.eq(track.trackId))
            .where(user.userId.eq(userId))
            .fetchOne()

        return Pair(
            result?.get(user.name),
            result?.get(track.trackName)
        )
    }


    private fun predicates(condition: ConditionDto): Array<BooleanExpression?> {
        return arrayOf(
            nameContains(condition.name),
            emailEquals(condition.email),
            trackIdEquals(condition.trackId),
            roleEquals(condition.role),
            statusEquals(condition.status),
            providerEquals(condition.provider),
            requestStatusEquals(condition.requestStatus)
        )
    }

    private fun providerEquals(provider: String?): BooleanExpression? {
        return if (!provider.isNullOrBlank()) user.provider.contains(provider) else null
    }

    private fun nameContains(name: String?): BooleanExpression? {
        return if (!name.isNullOrBlank()) user.name.contains(name) else null
    }

    private fun emailEquals(email: String?): BooleanExpression? {
        return if (!email.isNullOrBlank()) user.email.contains(email) else null
    }

    private fun trackIdEquals(trackId: Long?): BooleanExpression? {
        return trackId?.let { user.trackId.eq(it) }
    }

    private fun roleEquals(role: UserRole?): BooleanExpression? {
        return role?.let {
            user.role.eq(it)
        }
    }

    private fun statusEquals(status: UserStatus?): BooleanExpression? {
        return status?.let {
            user.status.eq(it)
        }
    }

    private fun requestStatusEquals(requestStatus: UserSignupStatus?): BooleanExpression? {
        return requestStatus?.let {
            requestSignup.requestStatus.eq(it)
        }
    }
}
