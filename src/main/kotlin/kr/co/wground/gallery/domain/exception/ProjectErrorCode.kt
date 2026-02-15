package kr.co.wground.gallery.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ProjectErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {

    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PJ-0001", "존재하지 않는 프로젝트입니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PJ-0002", "프로젝트에 대한 권한이 없습니다."),
    INVALID_PROJECT_TITLE(HttpStatus.BAD_REQUEST, "PJ-0003", "프로젝트 제목이 올바르지 않습니다. 1 ~ 100자 이내로 작성해주세요."),
    INVALID_GITHUB_URL(HttpStatus.BAD_REQUEST, "PJ-0004", "유효하지 않은 GitHub URL입니다."),
    INVALID_DEPLOY_URL(HttpStatus.BAD_REQUEST, "PJ-0005", "유효하지 않은 배포 URL입니다."),
    THUMBNAIL_REQUIRED(HttpStatus.BAD_REQUEST, "PJ-0006", "대표 이미지는 필수입니다."),
    TECH_STACK_REQUIRED(HttpStatus.BAD_REQUEST, "PJ-0007", "기술 스택은 최소 1개 이상 입력해야 합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PJ-0008", "존재하지 않는 팀원이 포함되어 있습니다."),
    PROJECT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "PJ-0009", "이미 삭제된 프로젝트입니다."),
}
