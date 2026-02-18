package kr.co.wground.study.domain.exception

import kr.co.wground.exception.ErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyReportApprovalHistory
import kr.co.wground.study.domain.TeamRetrospective
import kr.co.wground.study.domain.WeeklyActivities
import kr.co.wground.study.domain.WeeklyPlans
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
    STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "SD-0001", "해당 스터디는 모집 중이 아닙니다."),
    STUDY_CAPACITY_FULL(HttpStatus.BAD_REQUEST, "SD-0002", "해당 스터디의 최대 모집 정원이 전부 찼습니다."),
    STUDY_NAME_INVALID(HttpStatus.BAD_REQUEST, "SD-0003", "스터디의 이름이 유효하지 않습니다. 1 ~ 50자 이내로 작성해주세요."),
    STUDY_DESCRIPTION_INVALID(HttpStatus.BAD_REQUEST, "SD-0004", "스터디의 상세 설명이 유효하지 않습니다. 1 ~ 300자 이내로 작성해주세요."),
    STUDY_CAPACITY_TOO_SMALL(HttpStatus.BAD_REQUEST, "SD-0005", "스터티의 최소 모집 정원은 2명 이상 입니다."),
    STUDY_URL_INVALID(HttpStatus.BAD_REQUEST, "SD-0006", "정상적인 URL 구조가 아닙니다. 다시 입력해 주세요."),
    STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST, "SD-0007", "현재 모집된 인원이 수정하려는 모집 정원보다 많습니다."),
    STUDY_MUST_BE_RECRUITING_CLOSED_TO_START(HttpStatus.BAD_REQUEST, "SD-0008", "모집 종료 상태의 스터디만 진행 시작할 수 있습니다."),
    STUDY_CANNOT_MODIFY_IN_PROGRESS_OR_COMPLETED(HttpStatus.BAD_REQUEST, "SD-0009", "진행 중 또는 완료 상태의 스터디는 수정할 수 없습니다."),
    STUDY_CAPACITY_TOO_BIG(HttpStatus.BAD_REQUEST, "SD-0010", "스터티의 최대 모집 정원이 너무 많습니다."),
    STUDY_TAG_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST,"SD-0011","스터디 태그는 최대 ${Study.MAX_TAG_COUNT}개까지 가질수 있습니다."),
    STUDY_CANNOT_DELETE_IN_PROGRESS_OR_COMPLETED(HttpStatus.BAD_REQUEST,"SD-0012","진행 중 또는 완료 상태의 스터디는 삭제할 수 없습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST,"SD-0013","스터디장은 탈퇴할 수 없습니다."),
    STUDY_CANNOT_START_DUE_TO_NOT_ENOUGH_MEMBER(HttpStatus.CONFLICT, "SD-0014", "참여 인원이 최소 인원에 미달된 스터디는 진행 시작할 수 없습니다."),
    STUDY_BUDGET_EXPLAIN_INVALID(HttpStatus.BAD_REQUEST, "SD-0015", "지원 항목 설명이 유효하지 않습니다."),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST,"SD-0016","이미 참여 중인 스터디입니다."),
    NOT_PARTICIPATED_THAT_STUDY(HttpStatus.BAD_REQUEST, "SD-0017", "해당 스터디에 참여 중이지 않습니다."),
    RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_RECRUITING(HttpStatus.CONFLICT,"SD-0018","스터디 상태가 모집 중이 아닌 경우 신청을 취소할 수 없습니다."),
    CANNOT_FORCE_JOIN_IN_PROGRESS_OR_COMPLETED(HttpStatus.CONFLICT, "SD-0018", "진행 중 또는 완료 상태의 스터디에는 강제 참여할 수 없습니다."),
    WEEKLY_STUDY_PLANS_INVALID(HttpStatus.BAD_REQUEST, "SD-0019", "주차 별 스터디 계획 형식이 유효하지 않습니다. 주차 별 스터디 계획은 각각 ${WeeklyPlans.MIN_PLAN_LENGTH} 자 이상 ${WeeklyPlans.MAX_PLAN_LENGTH} 자 이하의 비어있지 않은 문자열이어야 합니다."),
    STUDY_REPORT_WEEKLY_ACTIVITIES_INVALID(HttpStatus.BAD_REQUEST, "SD-0020", "주차 별 활동 내역 형식이 유효하지 않습니다. 주차 별 활동 내역은 각각 ${WeeklyActivities.MIN_ACTIVITY_LENGTH} 자 이상 ${WeeklyActivities.MAX_ACTIVITY_LENGTH} 자 이하의 비어있지 않은 문자열이어야 합니다."),
    STUDY_REPORT_TEAM_RETROSPECTIVE_INVALID(HttpStatus.BAD_REQUEST, "SD-0021", "팀 회고 형식이 유효하지 않습니다. 팀 회고 항목은 각각 ${TeamRetrospective.MIN_RETROSPECTIVE_LENGTH} 자 이상 ${TeamRetrospective.MAX_RETROSPECTIVE_LENGTH} 자 이하의 비어있지 않은 문자열이어야 합니다."),
    STUDY_REPORT_STATUS_TRANSITION_INVALID(HttpStatus.BAD_REQUEST, "SD-0022", "스터디 결과 보고 결재 상태 전이가 유효하지 않습니다."),
    STUDY_REPORT_REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "SD-0023", "반려 사유는 필수 입력입니다."),
    STUDY_REPORT_REASON_TOO_LONG(HttpStatus.BAD_REQUEST, "SD-0024", "사유는 최대 ${StudyReportApprovalHistory.MAX_REASON_LENGTH}자까지 입력할 수 있습니다."),
    STUDY_REPORT_UPDATE_NOT_ALLOWED_FOR_STUDY_STATUS(HttpStatus.CONFLICT, "SD-0025", "스터디 결과 보고는 진행 중 또는 완료 상태의 스터디에서만 작성할 수 있습니다."),
    STUDY_REPORT_CANNOT_UPDATE_AFTER_APPROVED(HttpStatus.CONFLICT, "SD-0026", "결재 완료된 결과 보고는 수정할 수 없습니다. 취소 후 수정해 주세요."),
}
