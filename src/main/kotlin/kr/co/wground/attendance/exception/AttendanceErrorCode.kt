package kr.co.wground.attendance.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AttendanceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    ATTENDANCE_DAY_CANT_BEFORE_TODAY(HttpStatus.BAD_REQUEST,"AD-0001","출석 날짜는 현재보다 과거일 수 없습니다."),
}