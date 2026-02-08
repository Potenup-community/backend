package kr.co.wground.study_schedule.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyScheduleServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {

    //StudySchedule
    SCHEDULE_OVERLAP_WITH_NEXT(HttpStatus.BAD_REQUEST, "SS-0005", "현재 차수의 스터디 종료일은 다음 차수의 모집 시작일 보다 늦을 수 없습니다."),
    DUPLICATE_SCHEDULE_MONTH(HttpStatus.BAD_REQUEST,"SS-0006","해당 트랙에 이미 존재하는 차수 입니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND,"SS-0007","해당 일정이 존재하지 않습니다."),
    CANNOT_DELETE_SCHEDULE_WITH_STUDIES(HttpStatus.CONFLICT,"SS-0008","해당 일정을 참조하는 스터디가 있어 삭제할 수 없습니다."),
    SCHEDULE_OVERLAP_WITH_PREVIOUS(HttpStatus.BAD_REQUEST,"SS-0009","이전 차수의 스터디 종료일은 현재 차수의 스터디 모집 시작일 보다 늦을 수 없습니다."),
    NO_CURRENT_SCHEDULE(HttpStatus.BAD_REQUEST,"SS-0010","현재 스터디 신청 과정이 아닙니다."),
    INVALID_SCHEDULE_PARAMETER(HttpStatus.BAD_REQUEST,"SS-0011","스케줄 입력 값이 유효하지 않습니다."),
    RECRUIT_DUE_IS_OVER(HttpStatus.BAD_REQUEST, "SS-0012", "현재 스터디 모집 기간이 종료되었습니다."),

    STUDY_ALREADY_FINISH_TO_RECRUIT(HttpStatus.BAD_REQUEST,"SD-0013","해당 스터디의 모집기한이 이미 지났습니다.")
}