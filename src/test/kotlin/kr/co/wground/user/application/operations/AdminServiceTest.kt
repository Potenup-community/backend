package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.dto.DecisionDto
import kr.co.wground.user.application.operations.event.DecideUserStatusEvent
import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.utils.email.event.VerificationEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class AdminServiceTest {

    private val signupRepository = mock(RequestSignupRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val trackRepository = mock(TrackRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private lateinit var adminService: AdminServiceImpl

    @BeforeEach
    fun setUp() {
        adminService = AdminServiceImpl(signupRepository, userRepository,  eventPublisher)
    }

    @Test
    @DisplayName("가입 요청 승인 성공 - 상태 변경 이벤트와 이메일 발송 이벤트가 발행된다")
    fun decisionSignup_Success() {
        // given
        val userId = 1L
        val decisionDto = DecisionDto(
            userIds = listOf(userId),
            requestStatus = UserSignupStatus.ACCEPTED,
            role = UserRole.MEMBER
        )

        val requestSignup = RequestSignup(
            userId = userId,
            status = UserSignupStatus.PENDING,
        )

        val verificationTarget = VerificationEvent.VerificationTarget(
            email = "test@example.com",
            username = "testUser",
            trackName = "testTrack",
            approveAt = LocalDateTime.now()
        )

        `when`(signupRepository.findByUserIdIn(decisionDto.userIds)).thenReturn(listOf(requestSignup))
        `when`(userRepository.findAllApprovalTargets(decisionDto.userIds)).thenReturn(listOf(verificationTarget))

        // when
        // 성공 케이스는 예외 검증(assertThatThrownBy)으로 감싸지 않습니다.
        adminService.decisionSignup(decisionDto)

        // then
        // 1. 유저 상태 변경 이벤트 발행 확인
        verify(eventPublisher).publishEvent(any(DecideUserStatusEvent::class.java))

        // 2. 이메일 인증(승인) 이벤트 발행 확인
        verify(eventPublisher).publishEvent(any(VerificationEvent::class.java))
    }

    @Test
    @DisplayName("가입 요청 승인 실패 - 요청 데이터가 없으면 예외가 발생한다")
    fun decisionSignup_RequestNotFound() {
        // given
        val userId = 1L
        val decisionDto = DecisionDto(
            userIds = listOf(userId),
            requestStatus = UserSignupStatus.ACCEPTED,
            role = UserRole.MEMBER
        )

        `when`(signupRepository.findByUserIdIn(decisionDto.userIds)).thenReturn(emptyList())

        // when & then
        assertThatThrownBy { adminService.decisionSignup(decisionDto) }
            .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                // 리플렉션 대신 직접 프로퍼티에 접근하여 타입 안정성 확보
                assertThat(ex.code).isEqualTo(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND.code)
            }
    }

    /**
     * Kotlin Non-null 타입 파라미터에 Mockito any() 사용 시
     * null 리턴으로 인한 에러를 방지하기 위한 헬퍼 함수
     */
    private fun <T> any(type: Class<T>): T {
        ArgumentMatchers.any(type)
        return null as T
    }

    private fun <T> any(): T {
        ArgumentMatchers.any<T>()
        return null as T
    }
}