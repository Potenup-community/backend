package kr.co.wground.session.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.session.exception.SessionErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("AuthSession 도메인 테스트")
class AuthSessionTest {

    private fun createActiveSession(
        sessionId: String = "session-id",
        userId: Long = 1L,
        expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
    ) = AuthSession(
        sessionId = sessionId,
        userId = userId,
        deviceId = "device-abc",
        deviceName = "Chrome on Mac",
        userAgent = "Mozilla/5.0",
        ipAddress = "127.0.0.1",
        expiresAt = expiresAt,
    )

    @Nested
    @DisplayName("isActive()")
    inner class IsActive {

        @Test
        @DisplayName("ACTIVE 상태이고 만료 전이면 true를 반환한다")
        fun returnsTrue_whenActiveAndNotExpired() {
            val session = createActiveSession()
            assertThat(session.isActive()).isTrue()
        }

        @Test
        @DisplayName("ACTIVE 상태여도 expiresAt이 지났으면 false를 반환한다")
        fun returnsFalse_whenExpiredTime() {
            val session = createActiveSession(expiresAt = LocalDateTime.now().minusSeconds(1))
            assertThat(session.isActive()).isFalse()
        }

        @Test
        @DisplayName("REVOKED 상태면 만료 시간과 무관하게 false를 반환한다")
        fun returnsFalse_whenRevoked() {
            val session = createActiveSession()
            session.revoke()
            assertThat(session.isActive()).isFalse()
        }
    }

    @Nested
    @DisplayName("revoke()")
    inner class Revoke {

        @Test
        @DisplayName("ACTIVE 세션을 REVOKED로 전환하고 revokedAt을 기록한다")
        fun revokesActiveSession() {
            val session = createActiveSession()
            val before = LocalDateTime.now()

            session.revoke()

            assertThat(session.status).isEqualTo(SessionStatus.REVOKED)
            assertThat(session.revokedAt).isNotNull()
            assertThat(session.revokedAt).isAfterOrEqualTo(before)
        }

        @Test
        @DisplayName("이미 REVOKED 세션에 revoke()를 호출하면 SESSION_ALREADY_INACTIVE 예외를 던진다")
        fun throwsException_whenAlreadyRevoked() {
            val session = createActiveSession()
            session.revoke()

            assertThatThrownBy { session.revoke() }
                .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                    assertThat(ex.code).isEqualTo(SessionErrorCode.SESSION_ALREADY_INACTIVE.code)
                }
        }
    }

    @Nested
    @DisplayName("renew()")
    inner class Renew {

        @Test
        @DisplayName("REVOKED 세션을 ACTIVE로 재활성화하고 expiresAt을 갱신한다")
        fun renewsRevokedSession() {
            val session = createActiveSession()
            session.revoke()
            val newExpiry = LocalDateTime.now().plusDays(14)

            session.renew(newExpiry)

            assertThat(session.status).isEqualTo(SessionStatus.ACTIVE)
            assertThat(session.expiresAt).isEqualTo(newExpiry)
            assertThat(session.revokedAt).isNull()
        }
    }

    @Nested
    @DisplayName("touchLastSeen()")
    inner class TouchLastSeen {

        @Test
        @DisplayName("lastSeenAt이 현재 시각으로 갱신된다")
        fun updatesLastSeenAt() {
            val session = createActiveSession()
            val before = LocalDateTime.now()

            session.touchLastSeen()

            assertThat(session.lastSeenAt).isAfterOrEqualTo(before)
        }
    }
}
