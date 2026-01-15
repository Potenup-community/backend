package kr.co.wground.user.application.common
import kr.co.wground.exception.BusinessException
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kr.co.wground.user.application.operations.UserServiceImpl

class UserServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(userRepository)
    }

    @Test
    @DisplayName("내 정보 조회 성공 - 활성 유저")
    fun getMyInfo_Success() {
        // given
        val userId = 1L
        val user = User(
            userId = 1L,
            trackId = 1L,
            email = "test@test.com",
            name = "테스트",
            phoneNumber = "010-1234-5678",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // when
        val result = userService.getMyInfo(userId)

        // then
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.email).isEqualTo(user.email)
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 유저 없음")
    fun getMyInfo_UserNotFound() {
        // given
        val userId = 1L
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        // when & then
        assertThatThrownBy { userService.getMyInfo(userId) }
            .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                assertThat(ex.code).isEqualTo(UserServiceErrorCode.USER_NOT_FOUND.code)
            }
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 비활성 유저")
    fun getMyInfo_InactiveUser() {
        // given
        val userId = 1L
        val user = User(
            userId = 1L,
            trackId = 1L,
            email = "test@test.com",
            name = "테스트",
            phoneNumber = "010-1234-5678",
            provider = "GOOGLE",
            role = UserRole.MEMBER,
            status = UserStatus.BLOCKED
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // when & then
        assertThatThrownBy { userService.getMyInfo(userId) }
            .isInstanceOfSatisfying(BusinessException::class.java) { ex ->
                assertThat(ex.code).isEqualTo(UserServiceErrorCode.INACTIVE_USER.code)
            }
    }
}