package kr.co.wground.global.jwt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class RefreshTokenHasher(
    @Value("\${jwt.refresh-hash-secret}") private val secret: String
) {
    fun hash(token: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return Base64.getEncoder().encodeToString(mac.doFinal(token.toByteArray()))
    }
}