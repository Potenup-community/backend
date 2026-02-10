package kr.co.wground.study.domain.exception

import kr.co.wground.exception.ErrorCode
import kr.co.wground.study.domain.Study
import org.springframework.http.HttpStatus

enum class StudyDomainErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {

    //Tag
    TAG_LENGTH_INVALID_RANGE(HttpStatus.BAD_REQUEST, "TG-0001", "태그의 길이가 양식에 맞지 않습니다."),
    TAG_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "TG-0002", "태그의 입력 양식이 올바르지 않습니다."),

    //Study
    STUDY_NOT_PENDING(HttpStatus.BAD_REQUEST, "SD-0001", "해당 스터디는 모집중이 아닙니다."),
    STUDY_CAPACITY_FULL(HttpStatus.BAD_REQUEST, "SD-0002", "해당 스터디의 최대 모집 정원이 전부 찼습니다."),
    STUDY_NAME_INVALID(HttpStatus.BAD_REQUEST, "SD-0003", "스터디의 이름이 유효하지 않습니다. 1 ~ 50자 이내로 작성해주세요."),
    STUDY_DESCRIPTION_INVALID(HttpStatus.BAD_REQUEST, "SD-0004", "스터디의 상세 설명이 유효하지 않습니다. 1 ~ 300자 이내로 작성해주세요."),
    STUDY_CAPACITY_TOO_SMALL(HttpStatus.BAD_REQUEST, "SD-0005", "스터티의 최소 모집 정원은 2명 이상 입니다."),
    STUDY_URL_INVALID(HttpStatus.BAD_REQUEST, "SD-0006", "정상적인 URL 구조가 아닙니다. 다시 입력해 주세요."),
    STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST, "SD-0007", "현재 모집된 인원이 수정하려는 모집 정원보다 많습니다."),
    STUDY_MUST_BE_CLOSED_TO_APPROVE(HttpStatus.BAD_REQUEST, "SD-0008", "모집이 완료된 스터디만 결재 할 수 있습니다."),
    STUDY_CANNOT_MODIFY_AFTER_APPROVED(HttpStatus.BAD_REQUEST, "SD-0009", "스터디 정보 수정은 결재되기 이전에만 가능합니다."),
    STUDY_CAPACITY_TOO_BIG(HttpStatus.BAD_REQUEST, "SD-0010", "스터티의 최대 모집 정원이 너무 많습니다."),
    STUDY_TAG_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST,"SD-0011","스터디 태그는 최대 ${Study.MAX_TAG_COUNT}개까지 가질수 있습니다."),
    STUDY_CANT_DELETE_STATUS_APPROVED(HttpStatus.BAD_REQUEST,"SD-0012","결재 상신된 스터디는 삭제할 수 없습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST,"SD-0013","스터디장은 탈퇴할 수 없습니다."),
    STUDY_CANNOT_APPROVED_DUE_TO_NOT_ENOUGH_MEMBER(HttpStatus.CONFLICT, "SD-0014", "참여 인원 미달된 스터디는 승인할 수 없습니다."),
    STUDY_BUDGET_EXPLAIN_INVALID(HttpStatus.BAD_REQUEST, "SD-0015", "지원 항목 설명이 유효하지 않습니다."),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST,"SD-0016","이미 참여 중인 스터디입니다."),
    NOT_PARTICIPATED_THAT_STUDY(HttpStatus.BAD_REQUEST, "SD-0017", "해당 스터디에 참여 중이지 않습니다."),
    RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING(HttpStatus.CONFLICT,"SD-0018","스터디 상태가 PENDING 이 아닌 경우 신청을 취소할 수 없습니다.")
}
