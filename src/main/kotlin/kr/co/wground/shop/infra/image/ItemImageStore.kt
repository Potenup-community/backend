package kr.co.wground.shop.infra.image

import kr.co.wground.exception.BusinessException
import kr.co.wground.image.exception.UploadErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

@Component
class ItemImageStore(
    private val itemPolicy: ItemPolicy,
    private val itemImageValidator: ItemImageValidator,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object{
        const val DEFAULT_EXT = "bin"
    }

    fun store(file: MultipartFile): String {
        itemImageValidator.validate(file)

        val ext = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: DEFAULT_EXT
        val storedFileName = "${UUID.randomUUID()}.$ext"
        val targetPath = Path.of(itemPolicy.localDir, storedFileName)

        try {
            Files.createDirectories(targetPath.parent)
            file.inputStream.use { input ->
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            log.info("아이템 이미지 저장 완료: $storedFileName")
        } catch (e: Exception) {
            log.error("아이템 이미지 저장 실패: ${e.message}")
            throw BusinessException(UploadErrorCode.UPLOAD_IO_EXCEPTION)
        }

        return buildUrl(storedFileName)
    }

    fun delete(imageUrl: String) {
        val fileName = extractFileName(imageUrl) ?: return
        val targetPath = Path.of(itemPolicy.localDir, fileName)

        try {
            if (Files.deleteIfExists(targetPath)) {
                log.info("아이템 이미지 삭제 완료: $fileName")
            }
        } catch (e: Exception) {
            log.error("아이템 이미지 삭제 실패: ${e.message}")
        }
    }

    private fun buildUrl(fileName: String): String {
        val prefix = itemPolicy.webPathPrefix.trimEnd('/')
        return "$prefix/$fileName"
    }

    private fun extractFileName(imageUrl: String): String? {
        return imageUrl.substringAfterLast('/', "").ifBlank { null }
    }
}