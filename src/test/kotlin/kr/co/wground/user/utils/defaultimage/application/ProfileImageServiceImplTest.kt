package kr.co.wground.user.utils.defaultimage.application

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.co.wground.global.common.UserId
import kr.co.wground.image.validator.ImageUploadValidator
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
import java.util.*
import kotlin.io.path.createDirectories

@ExtendWith(MockKExtension::class)
class ProfileImageServiceImplTest {

    private lateinit var profileImageService: ProfileImageServiceImpl

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var imageUploadValidator: ImageUploadValidator

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
            imageUploadValidator,
            profilePolicy
        )
    }

    @Test
    @DisplayName("프로필 이미지가 정상적으로 로컬에 저장되고 User 정보가 업데이트되어야 한다")
    fun updateProfileImage_success() {
        // Given
        val userId: UserId = 1L
        
        // relaxed = true: 메서드 호출 시 별도 설정이 없으면 기본값 반환 (Unit 메서드는 그냥 실행됨)
        val user = mockk<User>(relaxed = true)

        // findByIdOrNull 내부에서 findById가 호출되므로 이를 Mocking
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { user.userProfile } returns UserProfile.default()
        every { imageUploadValidator.validate(any()) } just runs

        val multipartFile = MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake-image-content".toByteArray()
        )

        // When
        profileImageService.updateProfileImage(userId, multipartFile)

        // Then
        // 1. 파일 시스템 확인
        val userDir = tempDir.resolve(userId.toString())
        assertTrue(Files.exists(userDir), "User 디렉토리가 생성되어야 합니다")
        
        val files = Files.list(userDir).toList()
        assertEquals(1, files.size, "파일이 1개 생성되어야 합니다")
        assertEquals("fake-image-content", Files.readString(files[0]))

        // 2. User 업데이트 호출 검증 (MockK는 final 메서드도 검증 가능)
        verify(exactly = 1) { user.updateUserProfile(any()) }
    }

    @Test
    @DisplayName("이미지 삭제 시 실제 로컬 파일이 제거되고 User 정보가 초기화되어야 한다")
    fun deleteProfileImage_success() {
        // Given
        val userId: UserId = 2L
        val user = mockk<User>(relaxed = true)

        // 테스트용 파일 생성
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
        // 1. 파일 삭제 확인
        assertFalse(Files.exists(oldFile), "기존 파일은 삭제되어야 합니다")

        // 2. User 업데이트 호출 검증
        verify(exactly = 1) { user.updateUserProfile(any()) }
    }
}
