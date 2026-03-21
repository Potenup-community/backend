package kr.co.wground.token.infra

import kr.co.wground.token.domain.UserToken
import org.springframework.data.jpa.repository.JpaRepository

interface UserTokenJpaRepository : JpaRepository<UserToken, Long> {
    fun findByUserId(userId: Long): UserToken?
}