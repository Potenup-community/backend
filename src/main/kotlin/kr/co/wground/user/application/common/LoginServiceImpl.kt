package kr.co.wground.user.application.common

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.JwtProvider
import kr.co.wground.global.jwt.RefreshTokenHasher
import kr.co.wground.global.jwt.constant.TokenType
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
    val userRepository: UserRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val jwtProvider: JwtProvider,
    private val refreshTokenHasher: RefreshTokenHasher,
) : LoginService {

    @Transactional
    override fun login(loginRequest: LoginRequest): LoginResponse {
        val email = googleTokenVerifier.verify(loginRequest.idToken)

        val user = userRepository.findByEmail(email) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val accessToken = jwtProvider.createAccessToken(user.userId, user.role)

        val refreshToken = jwtProvider.createRefreshToken(user.userId, user.role)

        user.updateRefreshToken(refreshTokenHasher.hash(refreshToken))

        return LoginResponse(user.role, accessToken, refreshToken)
    }

    @Transactional
    override fun refreshAccessToken(request: String): TokenResponse {
        val userIdAndRole = jwtProvider.getUserIdAndRole(request, TokenType.REFRESH)

        val user = userRepository.findByIdOrNull(userIdAndRole.first)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val hashedRequest = refreshTokenHasher.hash(request)

        if (!user.validateRefreshToken(hashedRequest)) {
            throw BusinessException(UserServiceErrorCode.INVALID_REFRESH_TOKEN)
        }

        return if (user.refreshToken.token == hashedRequest) {
            val newAccessToken = jwtProvider.createAccessToken(user.userId, user.role)
            //val newRefreshToken = jwtProvider.createRefreshToken(user.userId, user.role)

            user.updateRefreshToken(refreshTokenHasher.hash(request))

            TokenResponse(user.userId, user.role, newAccessToken, request)
        } else {
            val newAccessToken = jwtProvider.createAccessToken(user.userId, user.role)

            TokenResponse(user.userId, user.role, newAccessToken, request)
        }
    }

    @Transactional
    override fun logout(userId: UserId) {
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        user.logout()
    }
}
