package kr.co.wground.study_schedule.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyScheduleDomainErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode  {

    //StudySchedule
    STUDY_CANT_START_AFTER_END_DATE(HttpStatus.BAD_REQUEST, "SS-0001", "모집 시작은 모집 종료보다 빨라야 합니다."),
    STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE(HttpStatus.BAD_REQUEST, "SS-0002", "모집 종료는 스터디 종료보다 빨라야 합니다."),
    STUDY_MONTH_ILLEGAL_RANGE(HttpStatus.BAD_REQUEST, "SS-0003", "유효하지 않은 차수입니다. 1 ~ 6차 사이여야 합니다."),
    STUDY_SCHEDULE_IS_NOT_IN_TRACK(HttpStatus.BAD_REQUEST,"SS-0004","해당 차수 일정은 현재 과정에 존재하지 않습니다."),
}