package kr.co.wground.study.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    STUDY_SCHEDULE_IS_NOT_IN_TRACK(HttpStatus.BAD_REQUEST, "SD-0014", "선택한 스터디 일정이 소속된 트랙에 존재하지 않습니다."),

}