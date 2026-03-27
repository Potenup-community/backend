package kr.co.wground.token.domain.repository

import kr.co.wground.token.domain.UserToken

interface UserTokenRepository {
    fun findByUserId(userId: Long): UserToken?
    fun findBySessionId(sessionId: String): UserToken?
    fun save(userToken: UserToken)
}