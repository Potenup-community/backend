package kr.co.wground.user.utils.defaultimage.application

import jakarta.transaction.Transactional
import kr.co.wground.global.common.UserId
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarProperties
import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import kr.co.wground.user.utils.defaultimage.infra.UserProfileRepository
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.UUID

@Service
class DefaultProfileService(
    private val properties: AvatarProperties,
    private val profileGenerator: ProfileGenerator,
    private val userProfileRepository: UserProfileRepository
) {
    companion object {
        private const val SVG_EXTENSION = ".svg"
        private const val WEBP_EXTENSION = ".webp"
        private const val DEFAULT_PROFILE = "default_profile_"
        private const val IMAGE_QUALITY = 300
    }
    @Transactional
    fun createDefaultProfile(userId: UserId, email: String, name: String) {
        val svgContent = profileGenerator.generateSvg(userId, name, email, properties.defaultSize)
        val fileName = "${UUID.randomUUID()}$SVG_EXTENSION"
        if(!Files.exists(properties.uploadPath)){
            Files.createDirectories(properties.uploadPath)
        }
        val filePath = properties.uploadPath.resolve(fileName)
        Files.writeString(filePath, svgContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        val accessUrl = "${properties.webPathPrefix}${userId}"

        val userProfile = UserProfile.create(
            userId = userId,
            originalProfileName = DEFAULT_PROFILE+userId,
            currentFileName = fileName,
            profileImageUrl = accessUrl,
            storagePath = properties.uploadPath.toString()
        )
        userProfileRepository.save(userProfile)
    }

}