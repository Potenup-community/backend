package kr.co.wground.token.infra

import kr.co.wground.token.domain.UserToken
import kr.co.wground.token.domain.repository.UserTokenRepository
import org.springframework.stereotype.Repository


@Repository
class UserTokenRepositoryImpl(
    private val userTokenJpaRepository: UserTokenJpaRepository
) : UserTokenRepository {
    override fun findByUserId(userId: Long): UserToken? =
        userTokenJpaRepository.findByUserId(userId)

    override fun findBySessionId(sessionId: String): UserToken? =
        userTokenJpaRepository.findBySessionId(sessionId)

    override fun save(userToken: UserToken) {
        userTokenJpaRepository.save(userToken)
    }
}