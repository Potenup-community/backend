package kr.co.wground.study.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    SCHEDULE_OVERLAP_WITH_NEXT(HttpStatus.BAD_REQUEST, "SD-0014", "현재 차수의 스터디 종료일은 다음 차수의 모집 시작일 보다 늦을 수 없습니다."),
    DUPLICATE_SCHEDULE_MONTH(HttpStatus.BAD_REQUEST,"SD-0015","해당 트랙에 이미 존재하는 차수 입니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND,"SD-0016","해당 일정이 존재하지 않습니다."),
    CANNOT_DELETE_SCHEDULE_WITH_STUDIES(HttpStatus.CONFLICT,"SD-0017","해당 일정을 참조하는 스터디가 있어 삭제할 수 없습니다."),
    SCHEDULE_OVERLAP_WITH_PREVIOUS(HttpStatus.BAD_REQUEST,"SD-0018","이전 차수의 스터디 종료일은 현재 차수의 스터디 모집 시작일 보다 늦을 수 없습니다."),

    //Study-Track
    TRACK_IS_NOT_ENROLLED(HttpStatus.BAD_REQUEST,"SDT-0001","해당 과정을 수강중인 상태가 아닙니다."),
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND,"SDT-0002","해당 과정을 찾을 수 없습니다.")
}