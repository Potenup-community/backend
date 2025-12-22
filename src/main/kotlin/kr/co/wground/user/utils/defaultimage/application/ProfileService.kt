package kr.co.wground.user.utils.defaultimage.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.response.ProfileResourceResponse
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarProperties
import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import org.springframework.core.io.FileSystemResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.UUID

@Service
@Transactional
class ProfileService(
    private val properties: AvatarProperties,
    private val profileGenerator: ProfileGenerator,
    private val userRepository: UserRepository,
) {
    companion object {
        private const val SVG_EXTENSION = ".svg"
        private const val WEBP_EXTENSION = ".webp"
        private const val DEFAULT_PROFILE = "default_profile_"
        private const val IMAGE_QUALITY = 300
    }

    fun createDefaultProfile(userId: UserId, email: String, name: String) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val svgContent = profileGenerator.generateSvg(userId, name, email, properties.defaultSize)
        val fileName = "${UUID.randomUUID()}$SVG_EXTENSION"

        if(!Files.exists(properties.uploadPath)){
            Files.createDirectories(properties.uploadPath)
        }

        val filePath = properties.uploadPath.resolve(fileName)
        Files.writeString(filePath, svgContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        val accessUrl = "${properties.webPathPrefix}/${userId}"

        val profile = UserProfile.create(
            originalProfileName = DEFAULT_PROFILE+userId,
            currentFileName = fileName,
            profileImageUrl = accessUrl,
            storagePath = properties.uploadPath.toString()
        )
        user.updateUserProfile(profile)
    }

    fun getProfileImageResource(userId: UserId): ProfileResourceResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val profile = user.userProfile
        val filePath = properties.uploadPath.resolve(profile.currentFileName)
        val primary = FileSystemResource(filePath)

        val resource = if (primary.exists()) {
            primary
        } else {
            FileSystemResource(properties.placeholderPath)
        }

        val filename = resource.filename ?: DEFAULT_AVATAR_PATH

        //미래를 위한 분기처리
        val contentType = when {
            filename.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
            filename.endsWith(".png", ignoreCase = true) -> "image/png"
            filename.endsWith(".jpg", ignoreCase = true) || filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> "application/octet-stream"
        }

        return ProfileResourceResponse(resource, contentType)
    }
}