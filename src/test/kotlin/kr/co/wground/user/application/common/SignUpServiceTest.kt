package kr.co.wground.user.application.common

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.SignUpRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher

class SignUpServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val googleTokenVerifier = mock(GoogleTokenVerifier::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private lateinit var signUpService: SignUpServiceImpl

    @BeforeEach
    fun setUp() {
        signUpService = SignUpServiceImpl(userRepository, googleTokenVerifier, eventPublisher)
    }

    @Test
    @DisplayName("회원가입 성공")
    fun addUser_Success() {
        // given
        val request = SignUpRequest("token", 1L, "테스트", "010-1234-5678", "GOOGLE")
        val email = "test@test.com"
        val savedUser = User(userId = 1L, trackId = 1L, email = email, name = "테스트", phoneNumber = "010-1234-5678", provider = "GOOGLE", role = UserRole.MEMBER)

        `when`(googleTokenVerifier.verify(anyString())).thenReturn(email)
        `when`(userRepository.existsUserByEmail(anyString())).thenReturn(false)
        `when`(userRepository.existsByPhoneNumber(anyString())).thenReturn(false)
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        // when
        signUpService.addUser(request)

        // then
        verify(userRepository).save(any(User::class.java))
        verify(eventPublisher, times(2)).publishEvent(any(Any::class.java))
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    fun addUser_AlreadySignedUser() {
        // given
        val request = SignUpRequest("token", 1L, "테스트", "010-1234-5678", "GOOGLE")
        val email = "test@test.com"

        `when`(googleTokenVerifier.verify(anyString())).thenReturn(email)
        `when`(userRepository.existsUserByEmail(anyString())).thenReturn(true)

        // when & then
        assertThatThrownBy { signUpService.addUser(request) }
            .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                assertThat(ex.code).isEqualTo(UserServiceErrorCode.ALREADY_SIGNED_USER.code)
            }
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 전화번호")
    fun addUser_DuplicatedPhoneNumber() {
        // given
        val request = SignUpRequest("token", 1L, "테스트", "010-1234-5678", "GOOGLE")
        val email = "test@test.com"

        `when`(googleTokenVerifier.verify(anyString())).thenReturn(email)
        `when`(userRepository.existsUserByEmail(anyString())).thenReturn(false)
        `when`(userRepository.existsByPhoneNumber(anyString())).thenReturn(true)

        // when & then
        assertThatThrownBy { signUpService.addUser(request) }
            .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                assertThat(ex.code).isEqualTo(UserServiceErrorCode.DUPLICATED_PHONE_NUMBER.code)
            }
    }
}