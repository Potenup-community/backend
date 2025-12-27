package kr.co.wground.user.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
class RefreshToken(
    @Column(name = "refresh_token")
    val token: String = "",

    @Column(name = "previous_refresh_token")
    val previousToken: String = "",

    @Column(name = "token_rotated_at")
    val rotatedAt: LocalDateTime? = null
) {
    private companion object {
        const val GRACE_PERIOD_SECONDS = 30L
    }

    fun isValid(hashedToken: String): Boolean {
        if (hashedToken.isBlank()) return false
        if (token.isBlank()) return false
        if (token == hashedToken) return true
        if (previousToken.isBlank()) return false

        val rotated = rotatedAt ?: return false
        return previousToken == hashedToken &&
                rotated.isAfter(LocalDateTime.now().minusSeconds(GRACE_PERIOD_SECONDS))
    }

    fun rotate(newToken: String): RefreshToken {
        return RefreshToken(
            token = newToken,
            previousToken = this.token,
            rotatedAt = LocalDateTime.now()
        )
    }
}