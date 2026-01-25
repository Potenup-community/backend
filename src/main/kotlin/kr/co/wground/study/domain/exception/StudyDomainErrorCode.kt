package kr.co.wground.study.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyDomainErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    //StudySchedule
    STUDY_CANT_START_AFTER_END_DATE(HttpStatus.BAD_REQUEST, "SS-0001", "모집 시작은 모집 종료보다 빨라야 합니다."),
    STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE(HttpStatus.BAD_REQUEST, "SS-0002", "모집 종료는 스터디 종료보다 빨라야 합니다."),
    STUDY_MONTH_ILLEGAL_RANGE(HttpStatus.BAD_REQUEST, "SS-0003", "유효하지 않은 차수입니다. 1 ~ 6차 사이여야 합니다."),
    STUDY_SCHEDULE_IS_NOT_IN_TRACK(HttpStatus.BAD_REQUEST,"SS-0004","해당 차수 일정은 현재 과정에 존재하지 않습니다."),

    //Tag
    TAG_LENGTH_INVALID_RANGE(HttpStatus.BAD_REQUEST, "TG-0001", "태그의 길이가 양식에 맞지 않습니다."),
    TAG_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "TG-0002", "태그의 입력 양식이 올바르지 않습니다."),

    //Study
    STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "SD-0001", "해당 스터디는 모집중이 아닙니다."),
    STUDY_CAPACITY_FULL(HttpStatus.BAD_REQUEST, "SD-0002", "해당 스터디의 최대 모집 정원이 전부 찼습니다."),
    STUDY_NAME_INVALID(HttpStatus.BAD_REQUEST, "SD-0003", "스터디의 이름이 유효하지 않습니다. 1 ~ 50자 이내로 작성해주세요."),
    STUDY_DESCRIPTION_INVALID(HttpStatus.BAD_REQUEST, "SD-0004", "스터디의 상세 설명이 유효하지 않습니다. 1 ~ 300자 이내로 작성해주세요."),
    STUDY_CAPACITY_TOO_SMALL(HttpStatus.BAD_REQUEST, "SD-0005", "스터티의 최소 모집 정원은 2명 이상 입니다."),
    STUDY_URL_INVALID(HttpStatus.BAD_REQUEST, "SD-0006", "정상적인 URL 구조가 아닙니다. 다시 입력해 주세요."),
    STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST, "SD-0007", "현재 모집된 인원이 수정하려는 모집 정원보다 많습니다."),
    STUDY_MUST_BE_CLOSED_TO_APPROVE(HttpStatus.BAD_REQUEST, "SD-0008", "모집이 완료된 스터디만 승인 할 수 있습니다."),
    STUDY_CANNOT_MODIFY_AFTER_DETERMINED(HttpStatus.BAD_REQUEST, "SD-0009", "스터디 정보 수정은 결재되기 이전에만 가능합니다."),
    STUDY_CAPACITY_TOO_BIG(HttpStatus.BAD_REQUEST, "SD-0010", "스터티의 최대 모집 정원이 너무 많습니다."),
    STUDY_TAG_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST,"SD-0011","스터디 태그는 최대 5개까지 가질수 있습니다."),
    STUDY_CANT_DELETE_STATUS_DETERMINE(HttpStatus.BAD_REQUEST,"SD-0012","결재 상신된 스터디는 삭제할 수 없습니다."),
    STUDY_ALREADY_FINISH_TO_RECRUIT(HttpStatus.BAD_REQUEST,"SD-0013","해당 스터디의 모집기한이 이미 지났습니다."),
    STUDY_MIN_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST,"SD-0014","스터디장은 탈퇴 할 수 없습니다."),
    STUDY_CANNOT_MODIFY_AFTER_DEADLINE(HttpStatus.BAD_REQUEST,"SD-0015","모집 마감 상태에서 수정할 수 없는 항목입니다."),

    //StudyRecruitment
    RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE(HttpStatus.BAD_REQUEST,"SR-0001","자기 소개는 2자 이상 200자 이내로 작성해주세요."),
    RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE(HttpStatus.BAD_REQUEST,"SR-0002","스터디가 확정되거나 반려된 상태에서는 신청 상태를 변경할 수 없습니다."),
    RECRUITMENT_INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST,"SR-0003","유효하지 않은 상태 변경입니다."),
    RECRUITMENT_APPEAL_EMPTY(HttpStatus.BAD_REQUEST,"SR-0004","자기소개가 입력되지 않았습니다."),

}
