package kr.co.wground.user.utils.defaultimage.domain

import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
class UserProfile(
    originalProfileName: String?,
    currentFileName: String?,
    var imageUrl: String,
    storagePath: String?,
    modifiedProfileAt: LocalDateTime = LocalDateTime.now(),
) {
    var originalProfileName: String? = null
        protected set

    var currentFileName: String? = null
        protected set

    var storagePath: String? = null
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
                imageUrl = profileImageUrl,
                storagePath = storagePath,
                modifiedProfileAt = LocalDateTime.now(),
            )
        }

        fun default(): UserProfile {
            return UserProfile(
                originalProfileName = null,
                currentFileName = null,
                imageUrl = "",
                storagePath = null,
                modifiedProfileAt = LocalDateTime.now()
            )
        }
    }

    fun getStoragesUrl(): String {
        return "$storagePath/$currentFileName"
    }
}
