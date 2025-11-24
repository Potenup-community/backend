package kr.co.wground.global.jwt

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class JwtProvider(@Value("\${jwt.secret}") secret: String) {
    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(), Jwts
            .SIG
            .HS256
            .key()
            .build()
            .getAlgorithm()
    )

    fun getUserId(token: String): Long {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .get("id", Long::class.java)
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
        userId: Long,
        expiredMs: Long
    ): String {
        return Jwts.builder()
            .claim("id", userId)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey)
            .compact()
    }
}
