package kr.co.wground.study.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyServiceErrorCode(
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

    //Study-Track
    TRACK_IS_NOT_ENROLLED(HttpStatus.BAD_REQUEST,"SDT-0001","해당 과정을 수강중인 상태가 아닙니다."),
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND,"SDT-0002","해당 과정을 찾을 수 없습니다."),

    //Study
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND,"SD-0015","해당 스터디를 찾을수 없습니다."),
    MAX_STUDY_EXCEEDED(HttpStatus.BAD_REQUEST,"SD-0016","스터디는 최대 2개까지 가입 가능합니다."),
    NOT_STUDY_LEADER(HttpStatus.FORBIDDEN,"SD-0017","스터디장만 사용할 수 있습니다."),

    //Tag
    TAG_CREATION_FAIL(HttpStatus.CONFLICT,"TG-0001","태그 생성 및 조회에 실패했습니다."),

    //StudyRecruitment
    NOT_RECRUITMENT_OWNER(HttpStatus.FORBIDDEN,"SR-0005","본인이 작성한 신청만 취소할 수 있습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST,"SR-0006","스터디장은 탈퇴할 수 없습니다."),
    TRACK_MISMATCH(HttpStatus.BAD_REQUEST,"SR-0007","신청자의 과정과 스터디의 과정이 일치하지 않습니다."),
    STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST,"SR-0008","해당 스터디는 모집중이 아닙니다."),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST,"SR-0009","이미 신청하거나 승인된 스터디입니다."),
    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"SR-0010","해당 스터디에 대한 신청기록을 찾을 수 없습니다."),
    STUDY_MONTH_IS_NOT_CURRENT_MONTH(HttpStatus.BAD_REQUEST,"SR-0011","신청한 차수는 현재 진행되는 차수에 해당하지 않습니다."),

}