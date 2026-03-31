package kr.co.wground.user.application.common

import com.github.benmanes.caffeine.cache.Cache
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.JwtProvider
import kr.co.wground.global.jwt.RefreshTokenHasher
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.session.application.SessionCommandService
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.session.domain.repository.AuthSessionRepository
import kr.co.wground.session.exception.SessionErrorCode
import kr.co.wground.token.domain.UserToken
import kr.co.wground.token.domain.repository.UserTokenRepository
import kr.co.wground.token.exception.TokenErrorCode
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.LoginResponse
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class LoginServiceImpl(
    private val userRepository: UserRepository,
    private val userTokenRepository: UserTokenRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val jwtProvider: JwtProvider,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val rotationCache: Cache<String, TokenResponse>,
    private val sessionCommandService: SessionCommandService,
    private val authSessionRepository: AuthSessionRepository,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshTokenExpiredMs: Long,
) : LoginService {

    @Transactional
    override fun login(loginRequest: LoginRequest, deviceContext: DeviceContext): LoginResponse {
        val email = googleTokenVerifier.verify(loginRequest.idToken)

        val user = userRepository.findByEmail(email)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiredMs / 1000)
        val session = sessionCommandService.upsertSession(user.userId, deviceContext, expiresAt)

        val accessToken = jwtProvider.createAccessToken(user.userId, user.role, session.sessionId)
        val refreshToken = jwtProvider.createRefreshToken(user.userId, user.role, session.sessionId)
        val hashedRefresh = refreshTokenHasher.hash(refreshToken)

        val existingToken = userTokenRepository.findBySessionId(session.sessionId)
        if (existingToken != null) {
            existingToken.rotate(hashedRefresh)
        } else {
            val newToken = UserToken(userId = user.userId, token = hashedRefresh)
            newToken.assignSession(session.sessionId)
            userTokenRepository.save(newToken)
        }

        return LoginResponse(user.role, accessToken, refreshToken)
    }

    @Transactional
    override fun refreshAccessToken(requestToken: String, deviceContext: DeviceContext): TokenResponse {
        val hashedRequest = refreshTokenHasher.hash(requestToken)

        rotationCache.getIfPresent(hashedRequest)?.let { return it }

        val tokenClaims = jwtProvider.parseTokenClaims(requestToken, TokenType.REFRESH)
        val (userId, _, sessionId) = tokenClaims

        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val session = sessionId?.let { authSessionRepository.findBySessionId(it) }
        if (session != null && !session.isActive()) {
            throw BusinessException(SessionErrorCode.SESSION_INACTIVE)
        }

        val userToken = (sessionId?.let { userTokenRepository.findBySessionId(it) }
            ?: userTokenRepository.findByUserId(userId))
            ?: throw BusinessException(TokenErrorCode.USER_REFRESH_TOKEN_NOT_FOUND)

        if (!userToken.isValid(hashedRequest)) {
            throw BusinessException(UserServiceErrorCode.INVALID_REFRESH_TOKEN)
        }

        val resolvedSessionId = session?.sessionId ?: sessionId ?: ""
        val newAccessToken = jwtProvider.createAccessToken(user.userId, user.role, resolvedSessionId)
        val newRefreshToken = jwtProvider.createRefreshToken(user.userId, user.role, resolvedSessionId)

        userToken.rotate(refreshTokenHasher.hash(newRefreshToken))
        session?.touchLastSeen()

        val response = TokenResponse(
            userId = user.userId,
            userRole = user.role,
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            sessionId = resolvedSessionId.takeIf { it.isNotBlank() },
        )
        rotationCache.put(hashedRequest, response)

        return response
    }

    @Transactional
    override fun logout(userId: UserId, sessionId: String) {
        if (sessionId.isNotBlank()) {
            runCatching { sessionCommandService.revokeSession(userId, sessionId) }
            userTokenRepository.findBySessionId(sessionId)?.clear()
        } else {
            userTokenRepository.findByUserId(userId)?.clear()
        }
    }
}
