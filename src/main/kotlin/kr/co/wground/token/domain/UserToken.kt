package kr.co.wground.token.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
@Table(name = "user_tokens")
class UserToken(
    val userId: UserId,
    token: String = "",
    previousToken: String = "",
    rotatedAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "refresh_token")
    var token: String = token
        protected set

    @Column(name = "previous_refresh_token")
    var previousToken: String = previousToken
        protected set

    @Column(name = "token_rotated_at")
    var rotatedAt: LocalDateTime? = rotatedAt
        protected set

    companion object {
        private const val GRACE_PERIOD_SECONDS = 30L
    }

    fun rotate(newToken: String) {
        previousToken = this.token
        token = newToken
        rotatedAt = LocalDateTime.now()
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

    fun clear() {
        this.previousToken = ""
        this.token = ""
    }
}