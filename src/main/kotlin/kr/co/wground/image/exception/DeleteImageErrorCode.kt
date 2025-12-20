package kr.co.wground.image.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST

enum class DeleteImageErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String
): ErrorCode {
    INVALID_RELATIVE_PATH(BAD_REQUEST, "UPL-0007", "파일의 경로가 유효하지 않습니다."),
    REFUSE_TO_DELETE_DIRECTORY(BAD_REQUEST, "UPL-0008", "디렉토리는 제거할 수 없습니다.")
}
