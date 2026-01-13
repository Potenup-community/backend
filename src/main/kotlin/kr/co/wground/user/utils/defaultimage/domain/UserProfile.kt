package kr.co.wground.user.utils.defaultimage.domain

import jakarta.persistence.Embeddable
import java.nio.file.Path
import java.time.LocalDateTime

@Embeddable
class UserProfile(
    originalProfileName: String? = null,
    currentFileName: String? = null,
    var imageUrl: String = "",
    storagePath: String? = null,
    modifiedProfileAt: LocalDateTime = LocalDateTime.now(),
) {
    var originalProfileName: String? = originalProfileName
        protected set

    var currentFileName: String? = currentFileName
        protected set

    var storagePath: String? = storagePath
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

    fun getStoragesUrl(): String? {
        val path = storagePath?.takeIf { it.isNotBlank() } ?: return null
        val fileName = currentFileName?.let { it.takeIf { it.isNotBlank() } } ?: return null

        return Path.of(path, fileName).toString()
    }
}
