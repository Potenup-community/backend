package kr.co.wground.session.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.session.domain.AuthSession
import kr.co.wground.session.domain.SessionStatus
import kr.co.wground.session.domain.event.SessionCreatedEvent
import kr.co.wground.session.domain.event.SessionRevokedEvent
import kr.co.wground.session.domain.repository.AuthSessionRepository
import kr.co.wground.session.exception.SessionErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

@DisplayName("SessionCommandService 테스트")
class SessionCommandServiceTest {

    private lateinit var authSessionRepository: AuthSessionRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var service: SessionCommandServiceImpl

    private val userId = 1L
    private val deviceContext = DeviceContext(
        deviceId = "device-abc",
        deviceName = "Chrome on Mac",
        userAgent = "Mozilla/5.0",
        ipAddress = "127.0.0.1",
    )
    private val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7)

    @BeforeEach
    fun setUp() {
        authSessionRepository = mock(AuthSessionRepository::class.java)
        eventPublisher = mock(ApplicationEventPublisher::class.java)
        service = SessionCommandServiceImpl(authSessionRepository, eventPublisher)
    }

    private fun createSession(
        sessionId: String = "sid-1",
        targetUserId: Long = userId,
        revoked: Boolean = false,
    ) = AuthSession(
        sessionId = sessionId,
        userId = targetUserId,
        deviceId = deviceContext.deviceId,
        deviceName = deviceContext.deviceName,
        userAgent = deviceContext.userAgent,
        ipAddress = deviceContext.ipAddress,
        expiresAt = expiresAt,
    ).also { if (revoked) it.revoke() }

    @Nested
    @DisplayName("upsertSession()")
    inner class UpsertSession {

        @Test
        @DisplayName("동일 (userId, deviceId) 세션이 없으면 새 세션을 생성하고 SessionCreatedEvent를 발행한다")
        fun createsNewSession_whenNoExisting() {
            `when`(authSessionRepository.findByUserIdAndDeviceId(userId, deviceContext.deviceId))
                .thenReturn(null)

            val result = service.upsertSession(userId, deviceContext, expiresAt)

            assertThat(result.userId).isEqualTo(userId)
            assertThat(result.status).isEqualTo(SessionStatus.ACTIVE)

            val eventCaptor = ArgumentCaptor.forClass(Any::class.java)
            verify(eventPublisher).publishEvent(eventCaptor.capture())
            assertThat(eventCaptor.value).isInstanceOf(SessionCreatedEvent::class.java)
        }

        @Test
        @DisplayName("동일 (userId, deviceId) 세션이 존재하면 renew()로 재활성화하고 생성 이벤트를 발행하지 않는다")
        fun renewsExistingSession_whenFound() {
            val existing = createSession("existing-sid")
            `when`(authSessionRepository.findByUserIdAndDeviceId(userId, deviceContext.deviceId))
                .thenReturn(existing)

            val result = service.upsertSession(userId, deviceContext, expiresAt)

            assertThat(result).isSameAs(existing)
            assertThat(result.status).isEqualTo(SessionStatus.ACTIVE)
            verifyNoInteractions(eventPublisher)
        }
    }

    @Nested
    @DisplayName("revokeSession()")
    inner class RevokeSession {

        @Test
        @DisplayName("본인 소유의 ACTIVE 세션을 철회하고 SessionRevokedEvent를 발행한다")
        fun revokesOwnSession() {
            val session = createSession("sid-1")
            `when`(authSessionRepository.findBySessionId("sid-1")).thenReturn(session)

            service.revokeSession(userId, "sid-1")

            assertThat(session.status).isEqualTo(SessionStatus.REVOKED)

            val eventCaptor = ArgumentCaptor.forClass(Any::class.java)
            verify(eventPublisher).publishEvent(eventCaptor.capture())
            assertThat(eventCaptor.value).isInstanceOf(SessionRevokedEvent::class.java)
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID는 SESSION_NOT_FOUND 예외를 발생시킨다")
        fun throwsException_whenSessionNotFound() {
            `when`(authSessionRepository.findBySessionId("not-exist")).thenReturn(null)

            assertThatThrownBy { service.revokeSession(userId, "not-exist") }
                .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                    assertThat(ex.code).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND.code)
                }
        }

        @Test
        @DisplayName("타인의 세션 ID로 요청하면 SESSION_FORBIDDEN 예외를 발생시킨다")
        fun throwsException_whenForbiddenSession() {
            val otherSession = createSession("sid-other", targetUserId = 999L)
            `when`(authSessionRepository.findBySessionId("sid-other")).thenReturn(otherSession)

            assertThatThrownBy { service.revokeSession(userId, "sid-other") }
                .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                    assertThat(ex.code).isEqualTo(SessionErrorCode.SESSION_FORBIDDEN.code)
                }
        }
    }

    @Nested
    @DisplayName("revokeOtherSessions()")
    inner class RevokeOtherSessions {

        @Test
        @DisplayName("현재 세션을 제외한 나머지 활성 세션이 모두 REVOKED 상태로 전환된다")
        fun revokesAllExceptCurrent() {
            val current = createSession("sid-current")
            val other1 = createSession("sid-other-1")
            val other2 = createSession("sid-other-2")
            `when`(authSessionRepository.findAllActiveByUserId(userId))
                .thenReturn(listOf(current, other1, other2))

            service.revokeOtherSessions(userId, "sid-current")

            assertThat(current.status).isEqualTo(SessionStatus.ACTIVE)
            assertThat(other1.status).isEqualTo(SessionStatus.REVOKED)
            assertThat(other2.status).isEqualTo(SessionStatus.REVOKED)
        }
    }
}
