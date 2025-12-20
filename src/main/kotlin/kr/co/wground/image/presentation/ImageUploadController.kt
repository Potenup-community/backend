package kr.co.wground.image.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.image.validator.ImageUploadValidator
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class ImageUploadController(
    private val validator: ImageUploadValidator,
    private val imageStorageService: ImageStorageService
) {
    @PostMapping("/upload", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(@RequestPart file: MultipartFile, ownerId: CurrentUserId) {
        validator.validate(file)

        imageStorageService.saveTemp(ownerId.value, file)
    }
}
