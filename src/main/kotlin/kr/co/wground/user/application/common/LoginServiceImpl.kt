package kr.co.wground.user.application.common

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.JwtProvider
import kr.co.wground.global.jwt.RefreshTokenHasher
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LoginServiceImpl(
    val userRepository: UserRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val jwtProvider: JwtProvider,
    private val refreshTokenHasher: RefreshTokenHasher,
) : LoginService {

    @Transactional
    override fun login(loginRequest: LoginRequest): TokenResponse {
        val email = googleTokenVerifier.verify(loginRequest.idToken)

        val user = userRepository.findByEmail(email) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val accessToken = jwtProvider.createAccessToken(user.userId)

        val refreshToken = jwtProvider.createRefreshToken(user.userId)

        user.updateRefreshToken(refreshTokenHasher.hash(refreshToken))

        return TokenResponse(accessToken, refreshToken)
    }

    @Transactional
    override fun refreshAccessToken(request: String): TokenResponse {

        val userId = jwtProvider.validateRefreshToken(request)

        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        val hashedRefreshToken = refreshTokenHasher.hash(request)
        if (user.refreshToken != hashedRefreshToken) {
            throw BusinessException(UserServiceErrorCode.INVALID_REFRESH_TOKEN)
        }

        val newAccessToken = jwtProvider.createAccessToken(user.userId)
        val newRefreshToken = jwtProvider.createRefreshToken(user.userId)

        user.updateRefreshToken(refreshTokenHasher.hash(newRefreshToken))

        return TokenResponse(newAccessToken, newRefreshToken)
    }

    @Transactional
    override fun logout(userId: UserId) {
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        user.logout()
    }
}
