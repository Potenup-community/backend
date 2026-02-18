package kr.co.wground.shop.infra.image

import kr.co.wground.exception.BusinessException
import kr.co.wground.image.exception.UploadErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ItemImageValidator(
    private val itemPolicy: ItemPolicy,
) {
    companion object {
        private const val HEADER_READ_SIZE = 12

        private val PNG_HEADER = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        private val GIF_HEADER = "GIF8".toByteArray()
        private val RIFF_HEADER = "RIFF".toByteArray()
        private val WEBP_MARKER = "WEBP".toByteArray()
        private val SVG_MARKERS = listOf("<svg", "<?xml")
    }

    fun validate(file: MultipartFile) {
        validateNotEmpty(file)
        validateFileSize(file)
        val ext = extractExtension(file)
        validateAllowedExtension(ext)
        validateMagicNumber(file, ext)
    }

    private fun validateNotEmpty(file: MultipartFile) {
        if (file.isEmpty) {
            throw BusinessException(UploadErrorCode.FILE_EMPTY_EXCEPTION)
        }
    }

    private fun validateFileSize(file: MultipartFile) {
        if (file.size > itemPolicy.maxBytes) {
            throw BusinessException(UploadErrorCode.FILE_TOO_LARGE_EXCEPTION)
        }
    }

    private fun validateAllowedExtension(ext: String) {
        println("ext=$ext, allowed=${itemPolicy.allowedExts}")
        if (ext !in itemPolicy.allowedExts) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_MIME_EXCEPTION)
        }
    }

    private fun validateMagicNumber(file: MultipartFile, ext: String) {
        val header = file.inputStream.use { it.readNBytes(HEADER_READ_SIZE) }

        val valid = when (ext) {
            "png" -> header.startsWith(PNG_HEADER)
            "gif" -> header.startsWith(GIF_HEADER)
            "webp" -> header.startsWith(RIFF_HEADER) && header.sliceOrNull(8, 12).contentEquals(WEBP_MARKER)
            "svg" -> {
                val text = file.inputStream.use { it.readNBytes(256).toString(Charsets.UTF_8).trimStart() }
                SVG_MARKERS.any { text.startsWith(it, ignoreCase = true) }
            }
            else -> false
        }

        if (!valid) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_FORMAT_EXCEPTION)
        }
    }

    private fun extractExtension(file: MultipartFile): String {
        return file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.ifBlank { null }
            ?: throw BusinessException(UploadErrorCode.UNSUPPORTED_MIME_EXCEPTION)
    }
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (this.size < prefix.size) return false
    return prefix.indices.all { this[it] == prefix[it] }
}

private fun ByteArray.sliceOrNull(from: Int, to: Int): ByteArray {
    if (this.size < to) return byteArrayOf()
    return this.sliceArray(from until to)
}