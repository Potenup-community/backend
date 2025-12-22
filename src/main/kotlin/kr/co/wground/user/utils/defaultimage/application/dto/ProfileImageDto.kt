package kr.co.wground.user.utils.defaultimage.application.dto

import java.time.LocalDateTime

data class ProfileImageDto(
    val id: Long,
    val userId: Long,
    val profileImageUrl: String,
    val originalProfileName: String,
    val currentFileName: String,
    val relativePath: String,
    val modifiedAt: LocalDateTime
)
