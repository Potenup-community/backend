package kr.co.wground.user.application.common

import com.github.benmanes.caffeine.cache.Cache
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.JwtProvider
import kr.co.wground.global.jwt.RefreshTokenHasher
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.token.domain.UserToken
import kr.co.wground.token.domain.repository.UserTokenRepository
import kr.co.wground.token.exception.TokenErrorCode
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.LoginResponse
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LoginServiceImpl(
    private val userRepository: UserRepository,
    private val userTokenRepository: UserTokenRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val jwtProvider: JwtProvider,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val rotationCache: Cache<String, TokenResponse>
) : LoginService {

    @Transactional
    override fun login(loginRequest: LoginRequest): LoginResponse {
        val email = googleTokenVerifier.verify(loginRequest.idToken)

        val user = userRepository.findByEmail(email)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val accessToken = jwtProvider.createAccessToken(user.userId, user.role)

        val refreshToken = jwtProvider.createRefreshToken(user.userId, user.role)

        userTokenRepository.findByUserId(user.userId).let { it?.rotate(refreshTokenHasher.hash(refreshToken)) }
            ?: userTokenRepository.save(
                UserToken(
                    userId = user.userId,
                    token = refreshTokenHasher.hash(refreshToken)
                )
            )

        return LoginResponse(user.role, accessToken, refreshToken)
    }

    @Transactional
    override fun refreshAccessToken(requestToken: String): TokenResponse {
        val hashedRequest = refreshTokenHasher.hash(requestToken)

        rotationCache.getIfPresent(hashedRequest)?.let {
            return it
        }

        val (userId, role) = jwtProvider.getUserIdAndRole(requestToken, TokenType.REFRESH)

        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val userToken = userTokenRepository.findByUserId(user.userId)
            ?: throw BusinessException(TokenErrorCode.USER_REFRESH_TOKEN_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        if (!userToken.isValid(hashedRequest)) {
            throw BusinessException(UserServiceErrorCode.INVALID_REFRESH_TOKEN)
        }

        val newAccessToken = jwtProvider.createAccessToken(user.userId, user.role)
        val newRefreshToken = jwtProvider.createRefreshToken(user.userId, user.role)

        userToken.rotate(refreshTokenHasher.hash(newRefreshToken))

        val response = TokenResponse(user.userId, user.role, newAccessToken, newRefreshToken)

        rotationCache.put(hashedRequest, response)

        return response
    }

    @Transactional
    override fun logout(userId: UserId) {
        val userToken = userTokenRepository.findByUserId(userId)
            ?: throw BusinessException(TokenErrorCode.USER_REFRESH_TOKEN_NOT_FOUND)

        userToken.clear()
    }
}
