package kr.co.wground.user.utils.defaultimage.application

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.image.exception.UploadErrorCode
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_FILE_NAME
import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import kr.co.wground.user.utils.defaultimage.policy.ProfilePolicy
import kr.co.wground.user.utils.defaultimage.validator.ProfileValidator
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class ProfileImageServiceImpl(
    private val userRepository: UserRepository,
    private val profileValidator: ProfileValidator,
    private val profilePolicy: ProfilePolicy,
) : ProfileImageService {

    companion object {
        val FILE_URL_REGEX = "(?<!http:|https:)//+".toRegex()
        private val log = LoggerFactory.getLogger(ProfileImageServiceImpl::class.java)
    }

    override fun updateProfileImage(userId: UserId, file: MultipartFile) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        profileValidator.validateImage(file)

        val (storedFileName, targetPath) = storeFile(userId, file)

        val newUserProfile = createNewUserProfile(userId, file.originalFilename, storedFileName, targetPath)

        deleteOldProfileFile(user)

        user.updateUserProfile(newUserProfile)
    }

    override fun deleteProfileImage(userId: UserId) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        deleteOldProfileFile(user)

        user.updateUserProfile(UserProfile.default())
    }


    private fun storeFile(userId: UserId, file: MultipartFile): Pair<String, Path> {
        val ext = file.originalFilename?.substringAfterLast('.', "").orEmpty().lowercase()
        val storedFileName = "${UUID.randomUUID()}.$ext"
        val targetPath = Path.of(profilePolicy.localDir, "$userId/$storedFileName")

        try {
            Files.createDirectories(targetPath.parent)
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            log.info("UserId: $userId, 프로필 파일 저장 완료: $storedFileName")
            return storedFileName to targetPath
        } catch (e: Exception) {
            log.error("UserId: $userId, 파일 저장 중 오류 발생: ${e.message}")
            throw BusinessException(UploadErrorCode.UPLOAD_IO_EXCEPTION)
        }
    }

    private fun createNewUserProfile(
        userId: UserId,
        originalName: String?,
        storedName: String,
        path: Path
    ): UserProfile {
        val imageUrl = "${profilePolicy.webPathPrefix}/$userId"
            .replace(FILE_URL_REGEX, "/")

        return UserProfile.create(
            originalProfileName = originalName ?: DEFAULT_FILE_NAME,
            currentFileName = storedName,
            profileImageUrl = imageUrl,
            storagePath = path.parent.toString()
        )
    }

    private fun deleteOldProfileFile(user: User) {
        val oldFilePath = user.userProfile.getStoragesUrl() ?: return

        try {
            if (Files.deleteIfExists(Path.of(oldFilePath))) {
                log.info("UserId: ${user.userId}, 이전 프로필 삭제 완료: $oldFilePath")
            }
        } catch (e: Exception) {
            log.error("UserId: ${user.userId}, 이전 프로필 삭제 실패: ${e.message}")
        }
    }
}