package kr.co.wground.user.infra

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.user.domain.QRequestSignup.requestSignup
import kr.co.wground.user.domain.QUser.user
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.UserListResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class CustomUserRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomUserRepository {
    override fun searchUsers(
        condition: UserSearchRequest,
        pageable: Pageable
    ): Page<UserListResponse> {

        val content = queryFactory
            .select(
                Projections.constructor(
                    UserListResponse::class.java,
                    user.userId,
                    user.name,
                    user.email,
                    user.phoneNumber,
                    user.trackId,
                    user.role,
                    user.status,
                    requestSignup.requestStatus,
                    user.createdAt
                )
            )
            .from(user)
            .leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId)) // 항상 Left Join 걸어도 성능 문제 거의 없음
            .where(
                nameContains(condition.name),
                emailContains(condition.email),
                trackIdEquals(condition.trackId),
                roleEquals(condition.role),
                statusEquals(condition.status),
                requestStatusEquals(condition.requestStatus)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(user.createdAt.desc())
            .fetch()

        val countQuery = queryFactory
            .select(user.count())
            .from(user)

        if (condition.requestStatus != null) {
            countQuery.leftJoin(requestSignup).on(user.userId.eq(requestSignup.userId))
        }

        countQuery.where(
            nameContains(condition.name),
            emailContains(condition.email),
            trackIdEquals(condition.trackId),
            roleEquals(condition.role),
            statusEquals(condition.status),
            requestStatusEquals(condition.requestStatus),
        )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun nameContains(name: String?): BooleanExpression? {
        return if (!name.isNullOrBlank()) user.name.contains(name) else null
    }

    private fun emailContains(email: String?): BooleanExpression? {
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
//
//    private fun isGraduatedIs(isGraduated : Boolean?): BooleanExpression? {
//        return isGraduated?.let{
//            true //TODO
//        }
//    }
}