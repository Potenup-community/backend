package kr.co.wground.user.utils.defaultimage.application

import kr.co.wground.global.common.UserId
import org.springframework.web.multipart.MultipartFile

interface ProfileImageService {
    fun updateProfileImage(userId: UserId, file: MultipartFile)
    fun deleteProfileImage(userId: UserId)
}