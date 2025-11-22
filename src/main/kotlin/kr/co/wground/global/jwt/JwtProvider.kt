package kr.co.wground.global.jwt

import io.jsonwebtoken.Jwts
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class JwtProvider(@Value("\${jwt.secret}") secret: String) {
    private val secretKey: SecretKey?

    init {
        this.secretKey = SecretKeySpec(
            secret.toByteArray(), Jwts
                .SIG
                .HS256
                .key()
                .build()
                .getAlgorithm()
        )
    }


    fun getUsername(token: String?): String {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("username", String::class.java)
    }

    fun getRole(token: String?): String {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String::class.java)
    }

    fun isExpired(token: String?): Boolean {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration()
            .before(Date())
    }

    fun createToken(
        userId: Long?,
        affiliationId: Long?,
        name: String?,
        email: String?,
        role: UserRole?,
        status: UserStatus?,
        expiredMs: Long
    ): String {
        return Jwts.builder()
            .claim("id", userId)
            .claim("affiliationId", affiliationId)
            .claim("username", name)
            .claim("email", email)
            .claim("role", role)
            .claim("status", status)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey)
            .compact()
    }
}