package kr.co.wground.image.application

import kr.co.wground.global.common.OwnerId
import kr.co.wground.image.application.dto.LocalStoredDto
import kr.co.wground.image.policy.UploadPolicy
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID.randomUUID

@Service
class ImageStorageService(
    private val props: UploadPolicy
) {
    fun saveTemp(ownerId: OwnerId, file: MultipartFile): LocalStoredDto {
        val id = randomUUID().toString()
        val ext = extFromMime(file.contentType) ?: "bin"

        val relativePath = relativePath(ownerId, id, ext)
        val targetPath = Path.of(props.localDir, relativePath)

        Files.createDirectories(targetPath.parent)

        file.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val url = uploadedUrl(relativePath)
        return LocalStoredDto(id = id, relativePath = relativePath, url = url)
    }

    private fun uploadedUrl(relativePath: String): String = "${props.publicBasePath.trimEnd('/')}/$relativePath"
    private fun relativePath(ownerId: OwnerId, id: String, ext: String) ="tmp/$ownerId/$id.$ext"

    private fun extFromMime(mime: String?): String? = when (mime?.lowercase()) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        else -> null
    }
}

