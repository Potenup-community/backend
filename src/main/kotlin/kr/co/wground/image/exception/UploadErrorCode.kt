package kr.co.wground.image.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST

enum class UploadErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String
): ErrorCode {
    FILE_EMPTY_EXCEPTION(BAD_REQUEST, "UPL-0001", "업로드할 파일이 비어 있습니다."),
    FILE_TOO_LARGE_EXCEPTION(BAD_REQUEST, "UPL-0002", "업로드할 파일이 너무 큽니다. (최대 5MB)"),
    UNSUPPORTED_MIME_EXCEPTION(BAD_REQUEST, "UPL-0003", "지원하지 않는 확장자입니다."),
    UNSUPPORTED_FORMAT_EXCEPTION(BAD_REQUEST, "UPL-0004", "허용되지 않은 이미지 포맷입니다."),
    INVALID_IMAGE_EXCEPTION(BAD_REQUEST, "UPL-0005", "유효한 이미지 파일이 아닙니다."),
    UPLOAD_IO_EXCEPTION(BAD_REQUEST, "UPL-0006", "파일 처리 중 오류가 발생했습니다."),
}
