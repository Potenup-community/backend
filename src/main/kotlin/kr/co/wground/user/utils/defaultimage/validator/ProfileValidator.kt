package kr.co.wground.user.utils.defaultimage.validator

import kr.co.wground.exception.BusinessException
import kr.co.wground.image.exception.UploadErrorCode
import kr.co.wground.user.utils.defaultimage.policy.ProfilePolicy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ProfileValidator(
    val profilePolicy: ProfilePolicy
) {
    companion object {
        private val log = LoggerFactory.getLogger(ProfileValidator::class.java)
    }

     fun validateImage(file: MultipartFile) {
        validateFileConstraints(file)

        val extension = extractExtension(file)
        validateExtensionAndMime(extension, file.contentType)

        validateImageHeader(file, extension)
    }

    private fun validateFileConstraints(file: MultipartFile) {
        if (file.isEmpty) throw BusinessException(UploadErrorCode.FILE_EMPTY_EXCEPTION)
        if (file.size > profilePolicy.maxBytes) {
            throw BusinessException(UploadErrorCode.FILE_TOO_LARGE_EXCEPTION)
        }
    }

    private fun extractExtension(file: MultipartFile): String {
        return file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: ""
    }

    private fun validateExtensionAndMime(ext: String, mime: String?) {
        if (!profilePolicy.allowedExts.contains(ext)) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_MIME_EXCEPTION)
        }
        if (mime == null || !mime.lowercase().startsWith("image/")) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_MIME_EXCEPTION)
        }
    }

    private fun validateImageHeader(file: MultipartFile, ext: String) {
        try {
            file.inputStream.use { inputStream ->
                val header = ByteArray(12)
                val readBytes = inputStream.read(header)

                if (readBytes < 4) throw BusinessException(UploadErrorCode.UNSUPPORTED_FORMAT_EXCEPTION)

                if (!isValidHeader(header, readBytes, ext)) {
                    log.warn("이미지 헤더 검증 실패: 확장자=$ext")
                    throw BusinessException(UploadErrorCode.UNSUPPORTED_FORMAT_EXCEPTION)
                }
            }
        } catch (e: Exception) {
            if (e is BusinessException) throw e
            log.error("이미지 헤더 검증 중 오류 발생: ${e.message}")
            throw BusinessException(UploadErrorCode.UPLOAD_IO_EXCEPTION)
        }
    }

    private fun isValidHeader(header: ByteArray, readBytes: Int, ext: String): Boolean {
        return when (ext) {
            "jpg", "jpeg" -> header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()
            "png" -> header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()
            "gif" -> {
                val s = String(header.copyOf(6))
                s == "GIF87a" || s == "GIF89a"
            }
            "webp" -> {
                if (readBytes < 12) false
                else {
                    val riff = String(header.copyOfRange(0, 4))
                    val webp = String(header.copyOfRange(8, 12))
                    riff == "RIFF" && webp == "WEBP"
                }
            }
            else -> {
                log.error("미지원 헤더 확장자 : $ext")
                false
            }
        }
    }
}