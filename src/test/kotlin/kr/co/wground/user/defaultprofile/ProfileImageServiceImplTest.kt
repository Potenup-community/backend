package kr.co.wground.user.defaultprofile

import kr.co.wground.user.utils.defaultimage.application.ProfileImageServiceImpl
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
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import kotlin.io.path.createDirectories


class ProfileImageServiceImplTest {

    private lateinit var profileImageService: ProfileImageServiceImpl

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val imageUploadValidator: ImageUploadValidator = mock(ImageUploadValidator::class.java)

    @TempDir
    lateinit var tempDir: Path

    private lateinit var profilePolicy: ProfilePolicy

    @BeforeEach
    fun setUp() {
        // 실제 파일 시스템 동작을 테스트하기 위해 @TempDir 경로를 정책에 주입
        profilePolicy = ProfilePolicy(
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
        val userId = 1L
        val user = mock(User::class.java)

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(user.userProfile).thenReturn(UserProfile.default())

        val originalFilename = "test-image.jpg"
        val content = "fake-image-content".toByteArray()
        val multipartFile = MockMultipartFile(
            "file",
            originalFilename,
            "image/jpeg",
            content
        )

        // When
        profileImageService.updateProfileImage(userId, multipartFile)

        // Then
        val userDir = tempDir.resolve(userId.toString())
        assertTrue(Files.exists(userDir), "User 디렉토리가 생성되어야 합니다")

        val files = Files.list(userDir).toList()
        assertEquals(1, files.size, "해당 폴더에 파일이 1개 존재해야 합니다")

        val savedFile = files[0]
        assertTrue(savedFile.toString().endsWith(".jpg"), "확장자가 유지되어야 합니다")
        assertEquals("fake-image-content", Files.readString(savedFile), "파일 내용이 일치해야 합니다")

    }

    @Test
    @DisplayName("이미지 삭제 시 실제 로컬 파일이 제거되고 User 정보가 초기화되어야 한다")
     fun deleteProfileImage_success() {
        // Given
        val userId = 2L
        val user = mock(User::class.java)

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

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(user.userProfile).thenReturn(userProfile)
        `when`(user.userId).thenReturn(userId)

        // When
        profileImageService.deleteProfileImage(userId)

        // Then
        assertFalse(Files.exists(oldFile), "기존 프로필 파일은 삭제되어야 합니다")

    }
}