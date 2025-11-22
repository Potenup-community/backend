package kr.co.wground.user.infra

import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.constant.UserSignupStatus
import org.springframework.data.jpa.repository.JpaRepository

interface RequestSignupRepository : JpaRepository<RequestSignup, Long> {
    fun findByEmail(email: String): RequestSignup?
    fun existsUserByEmail(email: String): Boolean
    fun findAllByRequestStatus(requestStatus : UserSignupStatus) : List<RequestSignup>
    fun existsByEmailAndRequestStatus(email: String, status : UserSignupStatus): Boolean
}