package kr.co.wground.user.utils.defaultimage.application

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import kr.co.wground.user.utils.defaultimage.policy.ProfilePolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kr.co.wground.user.utils.defaultimage.validator.ProfileValidator
import java.util.Optional

@ExtendWith(MockKExtension::class)
class ProfileImageServiceImplTest {

    private lateinit var profileImageService: ProfileImageServiceImpl

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var profileValidator: ProfileValidator

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        val profilePolicy = ProfilePolicy(
            localDir = tempDir.toString(),
            webPathPrefix = "/images/profile",
            maxBytes = 10 * 1024 * 1024,
            allowedExts = setOf("jpg", "png", "jpeg"),
            baseUrl = "http://localhost:8080"
        )

        profileImageService = ProfileImageServiceImpl(
            userRepository,
            profileValidator,
            profilePolicy
        )
    }

    @Test
    @DisplayName("프로필 이미지가 정상적으로 로컬에 저장되고 User 정보가 업데이트되어야 한다")
    fun updateProfileImage_success() {
        // Given
        val userId: UserId = 1L
        
        val user = mockk<User>(relaxed = true)

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { user.userProfile } returns UserProfile.default()
        every { profileValidator.validateImage(any()) } just runs

        val multipartFile = MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake-image-content".toByteArray()
        )

        // When
        profileImageService.updateProfileImage(userId, multipartFile)

        // Then
        val userDir = tempDir.resolve(userId.toString())
        assertTrue(Files.exists(userDir), "User 디렉토리가 생성되어야 합니다")
        
        val files = Files.list(userDir).toList()
        assertEquals(1, files.size, "파일이 1개 생성되어야 합니다")
        assertEquals("fake-image-content", Files.readString(files[0]))

        verify(exactly = 1) { user.updateUserProfile(any()) }
    }

    @Test
    @DisplayName("이미지 삭제 시 실제 로컬 파일이 제거되고 User 정보가 초기화되어야 한다")
    fun deleteProfileImage_success() {
        // Given
        val userId: UserId = 2L
        val user = mockk<User>(relaxed = true)

        val storedFileName = "old-image.png"
        val userDir = tempDir.resolve(userId.toString()).also { it.createDirectories() }
        val oldFile = userDir.resolve(storedFileName)
        Files.writeString(oldFile, "old-content")

        val userProfile = UserProfile.create(
            originalProfileName = "original.png",
            currentFileName = storedFileName,
            profileImageUrl = "/images/profile/$userId",
            storagePath = userDir.toString()
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { user.userProfile } returns userProfile
        every { user.userId } returns userId

        // When
        profileImageService.deleteProfileImage(userId)

        // Then
        assertFalse(Files.exists(oldFile), "기존 파일은 삭제되어야 합니다")
        verify(exactly = 1) { user.updateUserProfile(any()) }
    }
}
