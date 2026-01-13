package kr.co.wground.user.infra

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.QTrack.track
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.user.application.operations.constant.COUNT_DEFAULT_VALUE
import kr.co.wground.user.application.operations.constant.ID_DEFAULT_VALUE
import kr.co.wground.user.application.operations.constant.NOT_ASSOCIATE
import kr.co.wground.user.application.operations.dto.AcademicCount
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.application.operations.dto.RoleCount
import kr.co.wground.user.application.operations.dto.SignupCount
import kr.co.wground.user.application.operations.dto.StatusCount
import kr.co.wground.user.domain.QRequestSignup.requestSignup
import kr.co.wground.user.domain.QUser.user
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.dto.UserCountDto
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.utils.email.event.VerificationEvent
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
                    track.trackName,
                    user.role,
                    user.status,
                    requestSignup.requestStatus,
                    track.trackStatus,
                    user.provider,
                    user.createdAt,
                    user.modifiedAt,
                )
            )
            .from(user)
            .leftJoin(track).on(user.trackId.eq(track.trackId))
            .leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId))
            .where(
                *predicatesArray
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(user.createdAt.desc())
            .fetch()

        val countQuery = getUserCountQuery(condition, predicatesArray)

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: COUNT_DEFAULT_VALUE }
    }

    override fun calculateCounts(conditionDto: ConditionDto): UserCountDto {
        val basePredicates = predicates(conditionDto)

        return UserCountDto(
            totalCount = fetchTotalCount(basePredicates),
            signupSummary = fetchSignupSummary(conditionDto),
            statusSummary = conditionDto.status?.let { fetchStatusSummary(conditionDto) } ?: StatusCount.empty(),
            roleSummary = conditionDto.role?.let { fetchRoleSummary(conditionDto) } ?: RoleCount.empty(),
            academicSummary = conditionDto.isGraduated?.let { fetchTrackStatusSummary(conditionDto) }
                ?: AcademicCount.empty()
        )
    }

    private fun fetchTotalCount(predicates: Array<BooleanExpression?>): Long {
        return createBaseQuery()
            .select(user.count())
            .where(*predicates)
            .fetchOne() ?: COUNT_DEFAULT_VALUE
    }

    private fun fetchSignupSummary(condition: ConditionDto): SignupCount {
        val signupPredicates = predicates(condition.copy(requestStatus = null))

        return createBaseQuery()
            .select(
                Projections.constructor(
                    SignupCount::class.java,
                    Expressions.constant(true),
                    countIf(requestSignup.requestStatus.eq(UserSignupStatus.PENDING)),
                    countIf(requestSignup.requestStatus.eq(UserSignupStatus.ACCEPTED)),
                    countIf(requestSignup.requestStatus.eq(UserSignupStatus.REJECTED))
                )
            )
            .where(*signupPredicates)
            .fetchOne() ?: SignupCount.default()
    }

    private fun fetchStatusSummary(condition: ConditionDto): StatusCount {
        val statusPredicates = predicates(condition.copy(status = null))

        return createBaseQuery()
            .select(
                Projections.constructor(
                    StatusCount::class.java,
                    Expressions.constant(true),
                    countIf(user.status.eq(UserStatus.ACTIVE)),
                    countIf(user.status.eq(UserStatus.BLOCKED))
                )
            )
            .where(*statusPredicates)
            .fetchOne() ?: StatusCount.default()
    }

    private fun fetchTrackStatusSummary(condition: ConditionDto): AcademicCount {
        val trackStatusPredicates = predicates(condition.copy(isGraduated = null))

        return createBaseQuery()
            .select(
                Projections.constructor(
                    AcademicCount::class.java,
                    Expressions.constant(true),
                    countIf(track.trackStatus.eq(TrackStatus.GRADUATED)),
                    countIf(track.trackStatus.eq(TrackStatus.ENROLLED))
                )
            )
            .where(*trackStatusPredicates)
            .fetchOne() ?: AcademicCount.default()
    }

    private fun fetchRoleSummary(condition: ConditionDto): RoleCount {
        val rolePredicates = predicates(condition.copy(role = null))

        return createBaseQuery()
            .select(
                Projections.constructor(
                    RoleCount::class.java,
                    Expressions.constant(true),
                    countIf(user.role.eq(UserRole.MEMBER)),
                    countIf(user.role.eq(UserRole.INSTRUCTOR)),
                    countIf(user.role.eq(UserRole.ADMIN))
                )
            )
            .where(*rolePredicates)
            .fetchOne() ?: RoleCount.default()
    }


    private fun createBaseQuery(): JPAQuery<*> {
        return queryFactory
            .from(user)
            .leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId))
            .leftJoin(track).on(user.trackId.eq(track.trackId))
    }

    private fun countIf(condition: BooleanExpression): NumberExpression<Long> {
        return CaseBuilder()
            .`when`(condition).then(ID_DEFAULT_VALUE)
            .otherwise(COUNT_DEFAULT_VALUE)
            .sum()
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

    override fun findUserDisplayInfos(userIds: List<UserId>): Map<UserId, UserDisplayInfoDto> {
        val trackNameExpr = track.trackName.coalesce(NOT_ASSOCIATE)

        val results = queryFactory
            .select(
                Projections.constructor(
                    UserDisplayInfoDto::class.java,
                    user.userId,
                    user.name,
                    user.userProfile.imageUrl,
                    trackNameExpr,
                )
            )
            .from(user)
            .leftJoin(track).on(user.trackId.eq(track.trackId))
            .where(user.userId.`in`(userIds))
            .fetch()

        return results.associateBy { it.userId }
    }

    override fun findAllApprovalTargets(userIds: List<Long>): List<VerificationEvent.VerificationTarget> {
        return queryFactory
            .select(
                Projections.constructor(
                    VerificationEvent.VerificationTarget::class.java,
                    user.email,
                    user.name,
                    track.trackName.coalesce(NOT_ASSOCIATE),
                    Expressions.constant(LocalDateTime.now())
                )
            )
            .from(user)
            .leftJoin(track).on(user.trackId.eq(track.trackId))
            .where(user.userId.`in`(userIds))
            .fetch()
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
