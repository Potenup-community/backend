package kr.co.wground.image.presentation.response

import kr.co.wground.image.application.dto.LocalStoredDto

class UploadedImageResponse(
    val imageId: String,
    val relativePath: String,
    val url: String
)

fun LocalStoredDto.toResponse(): UploadedImageResponse {
    return UploadedImageResponse(
        imageId = imageId,
        relativePath = relativePath,
        url = url
    )
}
