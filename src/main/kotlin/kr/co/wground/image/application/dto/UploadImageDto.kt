package kr.co.wground.image.application.dto

import kr.co.wground.global.common.OwnerId
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

class UploadImageDto(
    val draftId: UUID,
    val ownerId: OwnerId,
    val imageFile: MultipartFile
) {
}
