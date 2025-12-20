package kr.co.wground.image.validator

import kr.co.wground.exception.BusinessException
import kr.co.wground.image.policy.UploadPolicy
import kr.co.wground.image.exception.UploadErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

@Component
class ImageUploadValidator(
    private val policy: UploadPolicy
) {
    companion object {
        const val GIF_TYPE = "image/gif"
        const val HEADER_LENGTH = 16
    }

    fun validate(file: MultipartFile) {
        validateEmptyFile(file)

        val mime = file.contentType?.lowercase()
        validateAllowedMimeType(mime)

        validateLargeFile(file, mime)

        if (!matchesMagicNumber(file, mime)) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_FORMAT_EXCEPTION)
        }
    }

    private fun matchesMagicNumber(file: MultipartFile, mime: String?): Boolean {
        val header = readHeader(file)
        if (header.isEmpty()) return false

        return when (mime) {
            "image/jpeg" -> startsWith(header, byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
            "image/png"  -> startsWith(header, byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
            "image/gif"  -> startsWith(header, "GIF87a".toByteArray()) || startsWith(header, "GIF89a".toByteArray())
            "image/webp" -> isWebp(header)
            else -> false
        }
    }

    private fun isWebp(header: ByteArray): Boolean {
        if (header.size < 12) return false
        val riff = header.copyOfRange(0, 4).toString(Charsets.US_ASCII)
        val webp = header.copyOfRange(8, 12).toString(Charsets.US_ASCII)
        return riff == "RIFF" && webp == "WEBP"
    }

    private fun readHeader(file: MultipartFile): ByteArray {
        try {
            BufferedInputStream(file.inputStream).use { bis ->
                val buf = ByteArray(HEADER_LENGTH)
                val read = bis.read(buf)
                if (read <= 0) return ByteArray(0)
                return if (read == HEADER_LENGTH) buf else buf.copyOf(read)
            }
        } catch (_: Exception) {
            throw BusinessException(UploadErrorCode.UPLOAD_IO_EXCEPTION)
        }
    }

    private fun startsWith(src: ByteArray, prefix: ByteArray): Boolean {
        if (src.size < prefix.size) return false
        for (i in prefix.indices) {
            if (src[i] != prefix[i]) return false
        }
        return true
    }

    private fun validateLargeFile(file: MultipartFile, mime: String?) {
        val size = file.size
        val isGif = mime == GIF_TYPE

        val max = if (isGif) policy.maxGifBytes else policy.maxImageBytes
        if (size > max) {
            throw BusinessException(UploadErrorCode.FILE_TOO_LARGE_EXCEPTION)
        }
    }

    private fun validateAllowedMimeType(mime: String?) {
        if (mime == null || mime !in policy.allowedMimeTypeSet) {
            throw BusinessException(UploadErrorCode.UNSUPPORTED_MIME_EXCEPTION)
        }
    }

    private fun validateEmptyFile(file: MultipartFile) {
        if (file.isEmpty) throw BusinessException(UploadErrorCode.FILE_EMPTY_EXCEPTION)
    }
}
