package kr.co.wground.user.utils.defaultimage.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Embeddable
class UserProfile (
    originalProfileName: String,
    currentFileName: String,
    val profileImageUrl: String,
    storagePath: String,
    modifiedAt: LocalDateTime = LocalDateTime.now(),
) {
    var originalProfileName: String = originalProfileName
        protected set

    var currentFileName: String = currentFileName
        protected set

    var storagePath: String = storagePath
        protected set

    var modifiedAt: LocalDateTime = modifiedAt
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
            )
        }
    }

    fun getStoragesUrl(): String {
        return "$storagePath/$currentFileName"
    }

    fun updateProfile(
        newOriginalProfileName: String,
        newCurrentProfileName: String,
        newStoragePath: String
    ) {
        this.originalProfileName = newOriginalProfileName
        this.currentFileName = newCurrentProfileName
        this.storagePath = newStoragePath
        updateModifiedAt(LocalDateTime.now())
    }

    fun updateModifiedAt(modifiedAt: LocalDateTime) {
        this.modifiedAt = modifiedAt
    }
}