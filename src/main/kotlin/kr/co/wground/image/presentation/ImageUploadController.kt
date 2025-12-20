package kr.co.wground.image.presentation

import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.image.application.dto.UploadImageDto
import kr.co.wground.image.presentation.request.UploadImageRequest
import kr.co.wground.image.validator.ImageUploadValidator
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/files")
class ImageUploadController(
    private val imageStorageService: ImageStorageService
) {
    @PostMapping("/upload", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @Valid@RequestBody request: UploadImageRequest,
        @RequestPart file: MultipartFile,
        ownerId: CurrentUserId
    ) {
        imageStorageService.saveTemp(UploadImageDto(request.draftId, ownerId.value, file))
    }
}
