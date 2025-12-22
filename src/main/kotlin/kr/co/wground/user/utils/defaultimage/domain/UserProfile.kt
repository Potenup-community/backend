package kr.co.wground.user.utils.defaultimage.domain

import jakarta.persistence.Embeddable
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_FILE_NAME
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_PROFILE_NAME
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_STORAGE_PATH
import java.time.LocalDateTime

@Embeddable
class UserProfile(
    originalProfileName: String,
    currentFileName: String,
    val profileImageUrl: String,
    storagePath: String,
    modifiedProfileAt: LocalDateTime = LocalDateTime.now(),
) {
    var originalProfileName: String = originalProfileName
        protected set

    var currentFileName: String = currentFileName
        protected set

    var storagePath: String = storagePath
        protected set

    var modifiedProfileAt: LocalDateTime = modifiedProfileAt
        protected set

    companion object {

        fun create(
            originalProfileName: String,
            currentFileName: String,
            profileImageUrl: String,
            storagePath: String
        ): UserProfile {
            return UserProfile(
                originalProfileName = originalProfileName,
                currentFileName = currentFileName,
                profileImageUrl = profileImageUrl,
                storagePath = storagePath,
                modifiedProfileAt = LocalDateTime.now(),
            )
        }

        fun default(): UserProfile {
            return UserProfile(
                originalProfileName = DEFAULT_PROFILE_NAME,
                currentFileName = DEFAULT_FILE_NAME,
                profileImageUrl = DEFAULT_AVATAR_PATH,
                storagePath = DEFAULT_STORAGE_PATH,
                modifiedProfileAt = LocalDateTime.now()
            )
        }
    }

    fun getStoragesUrl(): String {
        return "$storagePath/$currentFileName"
    }
}
