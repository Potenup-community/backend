package kr.co.wground.user.utils.defaultimage.application.dto

import kr.co.wground.user.utils.defaultimage.domain.UserProfile


object ProfileImageMapper {
    fun toDto(entity: UserProfile) = ProfileImageDto(
        id = entity.id,
        userId = entity.userId,
        profileImageUrl = entity.profileImageUrl,
        originalProfileName = entity.originalProfileName,
        currentFileName = entity.currentFileName,
        relativePath= entity.storagePath,
        modifiedAt = entity.modifiedAt
    )
    fun toEntity(dto: ProfileImageDto) = UserProfile.create(
        userId = dto.userId,
        profileImageUrl = dto.profileImageUrl,
        originalProfileName = dto.originalProfileName,
        currentFileName = dto.currentFileName,
        relativePath = dto.relativePath
    )
}