package kr.co.wground.api.user.repository

import kr.co.wground.api.user.domain.RequestSignup
import org.springframework.data.jpa.repository.JpaRepository

interface RequestSignupRepository : JpaRepository<RequestSignup, Long> {
    fun findByEmail(email: String): RequestSignup?
    fun existsRequestSignupByEmail(email: String): Boolean
}