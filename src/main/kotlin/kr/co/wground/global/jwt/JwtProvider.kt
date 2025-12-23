package kr.co.wground.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.user.application.exception.UserServiceErrorCode
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
    private val accessTokenExpired: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshTokenExpired: Long,
    ) {

    private companion object {
        private const val CLAIM_USER_ID = "userId"
        private const val CLAIM_TOKEN_TYPE = "tokenType"
    }

    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(), Jwts
            .SIG
            .HS256
            .key()
            .build()
            .getAlgorithm()
    )

    fun createAccessToken(userId: UserId): String {
        return createToken(userId, TokenType.ACCESS, accessTokenExpired)
    }


    fun createRefreshToken(userId: UserId): String {
        return createToken(userId, TokenType.REFRESH, refreshTokenExpired)
    }

    fun validateAccessToken(token: String): Long {
        val claims = parseClaims(token, TokenType.ACCESS, UserServiceErrorCode.INVALID_ACCESS_TOKEN)
        return extractUserId(claims, UserServiceErrorCode.INVALID_ACCESS_TOKEN)
    }

    fun validateRefreshToken(token: String): Long {
        val claims = parseClaims(token, TokenType.REFRESH, UserServiceErrorCode.INVALID_REFRESH_TOKEN)
        return extractUserId(claims, UserServiceErrorCode.INVALID_REFRESH_TOKEN)
    }

    private fun createToken(userId: UserId, tokenType: TokenType, expiredMs: Long): String {
        return Jwts.builder()
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_TOKEN_TYPE, tokenType.name)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey)
            .compact()
    }


    private fun parseClaims(
        token: String,
        expectedType: TokenType,
        invalidErrorCode: UserServiceErrorCode
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

        val tokenType = runCatching { (claims[CLAIM_TOKEN_TYPE] as? String)?.let { TokenType.valueOf(it) } }.getOrNull()
        if (tokenType != expectedType) {
            throw BusinessException(invalidErrorCode)
        }
        return claims
    }

    private fun extractUserId(claims: Claims, invalidErrorCode: UserServiceErrorCode): Long {
        return claims.get(CLAIM_USER_ID, Number::class.java)?.toLong()
            ?: throw BusinessException(invalidErrorCode)
    }

}
