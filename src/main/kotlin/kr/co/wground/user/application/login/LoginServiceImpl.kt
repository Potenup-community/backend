package kr.co.wground.user.application.login

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.global.jwt.JwtProvider
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.LoginResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LoginServiceImpl(
    val userRepository: UserRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val jwtProvider: JwtProvider,
    @Value("\${jwt.expiration-ms}")
    private val accessTokenExpiredMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshTokenExpiredMs: Long
) : LoginService {



    @Transactional
    override fun login(loginRequest: LoginRequest): LoginResponse {
        val email = googleTokenVerifier.verify(loginRequest.idToken)

        val user = userRepository.findByEmail(email) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }

        val accessToken = jwtProvider.createToken(
            user.userId,
            accessTokenExpiredMs
        )

        val refreshToken = jwtProvider.createToken(
            user.userId,
            refreshTokenExpiredMs
        )
        return LoginResponse(accessToken, refreshToken)
    }
}