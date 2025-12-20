package kr.co.wground.image.presentation.request

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class UploadImageRequest(
    @field:NotNull(message = "드래프트 아이디를 넣어주세요.")
    val draftId: UUID
) {
}
