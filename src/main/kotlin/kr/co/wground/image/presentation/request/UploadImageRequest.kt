package kr.co.wground.image.presentation.request

import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class UploadImageRequest(
    @field:NotNull(message = "드래프트 아이디를 넣어주세요.")
    val draftId: UUID,
    @field:NotNull(message = "이미지 파일을 추가해주세요")
    val file: MultipartFile
) {
}
