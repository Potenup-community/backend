package kr.co.wground.study.application.exception

import kr.co.wground.exception.ErrorCode
import kr.co.wground.study.application.RecruitValidator
import org.springframework.http.HttpStatus

enum class StudyServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {

    //Study-Track
    TRACK_IS_NOT_ENROLLED(HttpStatus.BAD_REQUEST,"SDT-0001","해당 과정을 수강중인 상태가 아닙니다."),
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND,"SDT-0002","해당 과정을 찾을 수 없습니다."),

    //Study
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND,"SD-0019","해당 스터디를 찾을수 없습니다."),
    MAX_STUDY_EXCEEDED(HttpStatus.BAD_REQUEST,"SD-0020","스터디는 최대 ${RecruitValidator.MAX_STUDY_CAN_ENROLLED}개까지 가입 가능합니다."),
    NOT_STUDY_LEADER(HttpStatus.FORBIDDEN,"SD-0021","스터디장만 사용할 수 있습니다."),
    ONLY_ADMIN_AND_LEADER_COULD_DELETE_STUDY(HttpStatus.UNAUTHORIZED, "SD-0022", "삭제 권한이 없습니다."),

    //Tag
    TAG_CREATION_FAIL(HttpStatus.CONFLICT,"TG-0001","태그 생성 및 조회에 실패했습니다."),

    //StudyRecruitment
    NOT_RECRUITMENT_OWNER(HttpStatus.FORBIDDEN,"SR-0001","본인이 작성한 신청만 취소할 수 있습니다."),
    TRACK_MISMATCH(HttpStatus.BAD_REQUEST,"SR-0002","신청자의 과정과 스터디의 과정이 일치하지 않습니다."),
    STUDY_NOT_PENDING(HttpStatus.BAD_REQUEST,"SR-0003","해당 스터디는 모집중이 아닙니다."),
    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"SD-0004","해당 스터디 참여 건을 찾을 수 없습니다."),
    STUDY_MONTH_IS_NOT_CURRENT_MONTH(HttpStatus.BAD_REQUEST,"SR-0005","신청한 차수는 현재 진행되는 차수에 해당하지 않습니다."),
    GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY(HttpStatus.FORBIDDEN,"SR-0006","수료생은 지원할 수 없는 스터디입니다."),
}