package kr.co.wground.user.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.constant.UserSignupStatus
import org.springframework.data.jpa.repository.JpaRepository

interface RequestSignupRepository : JpaRepository<RequestSignup, Long> {
    fun findByUserId(userId: UserId): RequestSignup?
    fun existsByUserId(userId: UserId): Boolean
    fun findAllByRequestStatus(requestStatus : UserSignupStatus) : List<RequestSignup>
}
