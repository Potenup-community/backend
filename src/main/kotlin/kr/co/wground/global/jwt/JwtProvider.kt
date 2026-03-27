package kr.co.wground.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtProvider(
    @Value("\${jwt.secret}")
    private val secret: String,
    @Value("\${jwt.expiration-ms}")
    private val accessTokenExpiredMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshTokenExpiredMs: Long,
) {
    private companion object {
        const val CLAIM_USER_ID = "userId"
        const val CLAIM_TOKEN_TYPE = "tokenType"
        const val CLAIM_USER_ROLE = "role"
        const val CLAIM_SESSION_ID = "sid"
    }

    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(),
        Jwts.SIG.HS256.key().build().algorithm,
    )

    // ── 토큰 발급 ────────────────────────────────────────────────────────────────

    fun createAccessToken(userId: UserId, role: UserRole, sessionId: String = ""): String =
        buildToken(userId, role, TokenType.ACCESS, accessTokenExpiredMs, sessionId)

    fun createRefreshToken(userId: UserId, role: UserRole, sessionId: String = ""): String =
        buildToken(userId, role, TokenType.REFRESH, refreshTokenExpiredMs, sessionId)

    private fun buildToken(
        userId: UserId,
        role: UserRole,
        tokenType: TokenType,
        expiredMs: Long,
        sessionId: String,
    ): String = Jwts.builder()
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_USER_ROLE, role.name)
        .claim(CLAIM_TOKEN_TYPE, tokenType.name)
        .claim(CLAIM_SESSION_ID, sessionId)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(Date(System.currentTimeMillis() + expiredMs))
        .signWith(secretKey)
        .compact()

    // ── 토큰 파싱 ────────────────────────────────────────────────────────────────

    /**
     * 토큰에서 인증에 필요한 claims를 추출한다.
     * access / refresh 토큰 모두에 사용 가능하다.
     * 만료된 access 토큰은 [ExpiredJwtException]을 발생시켜 상위에서 refresh 처리하도록 한다.
     */
    fun parseTokenClaims(token: String, expectedType: TokenType): ParsedTokenClaims {
        val errorCode = when (expectedType) {
            TokenType.ACCESS -> UserServiceErrorCode.INVALID_ACCESS_TOKEN
            TokenType.REFRESH -> UserServiceErrorCode.INVALID_REFRESH_TOKEN
        }
        val claims = parseClaims(token, expectedType, errorCode)
        return ParsedTokenClaims(
            userId = extractUserId(claims, errorCode),
            role = claims.get(CLAIM_USER_ROLE, String::class.java)
                ?: throw BusinessException(errorCode),
            sessionId = claims.get(CLAIM_SESSION_ID, String::class.java)
                ?.takeIf { it.isNotBlank() },
        )
    }

    private fun parseClaims(
        token: String,
        expectedType: TokenType,
        invalidErrorCode: UserServiceErrorCode,
    ): Claims {
        val claims = try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(invalidErrorCode)
        }

        val tokenType = runCatching {
            (claims[CLAIM_TOKEN_TYPE] as? String)?.let { TokenType.valueOf(it) }
        }.getOrNull()

        if (tokenType != expectedType) throw BusinessException(invalidErrorCode)

        return claims
    }

    private fun extractUserId(claims: Claims, invalidErrorCode: UserServiceErrorCode): UserId =
        claims.get(CLAIM_USER_ID, Number::class.java)?.toLong()
            ?: throw BusinessException(invalidErrorCode)
}

/**
 * 파싱된 JWT claims를 담는 구조체.
 * [sessionId]는 신규 토큰에만 존재하며, 구형 토큰 호환을 위해 nullable이다.
 */
data class ParsedTokenClaims(
    val userId: UserId,
    val role: String,
    val sessionId: String?,
)
