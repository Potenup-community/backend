package kr.co.wground.user.utils.defaultimage.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
class UserProfile private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: UserId,
    originalProfileName: String,
    currentFileName: String,
    profileImageUrl: String,
    val relativePath: String,
    modifiedAt: LocalDateTime = LocalDateTime.now(),
) {
    var originalProfileName: String = originalProfileName
        protected set

    var currentFileName: String = currentFileName
        protected set

    var profileImageUrl: String = profileImageUrl
        protected set

    var modifiedAt: LocalDateTime = modifiedAt
        protected set

    companion object {
        fun create(
            userId: UserId,
            originalProfileName: String,
            currentFileName: String,
            profileImageUrl: String,
            relativePath: String
        ): UserProfile {
            return UserProfile(
                userId = userId,
                originalProfileName = originalProfileName,
                currentFileName = currentFileName,
                profileImageUrl = profileImageUrl,
                relativePath = relativePath,
            )
        }
    }

    fun getAccessUrl(): String{
        return "$relativePath/$currentFileName"
    }

    fun updateProfile(newOriginalProfileName: String, newCurrentProfileName: String, newProfileImageUrl: String) {
        this.originalProfileName = newOriginalProfileName
        this.currentFileName = newCurrentProfileName
        this.profileImageUrl = newProfileImageUrl
        updateModifiedAt(LocalDateTime.now())
    }

    fun updateModifiedAt(modifiedAt: LocalDateTime) {
        this.modifiedAt = modifiedAt
    }
}