package kr.co.wground.study.docs

object StudySwaggerErrorExample {
    object Study {
        const val STUDY_NOT_RECRUITING = """{
            "code": "SD-0001",
            "message": "해당 스터디는 모집중이 아닙니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CAPACITY_FULL = """{
            "code": "SD-0002",
            "message": "해당 스터디의 최대 모집 정원이 전부 찼습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_NAME_INVALID = """{
            "code": "SD-0003",
            "message": "스터디의 이름이 유효하지 않습니다. 1 ~ 50자 이내로 작성해주세요.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_DESCRIPTION_INVALID = """{
            "code": "SD-0004",
            "message": "스터디의 상세 설명이 유효하지 않습니다. 1 ~ 300자 이내로 작성해주세요.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CAPACITY_TOO_SMALL = """{
            "code": "SD-0005",
            "message": "스터티의 최소 모집 정원은 2명 이상 입니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_URL_INVALID = """{
            "code": "SD-0006",
            "message": "정상적인 URL 구조가 아닙니다. 다시 입력해 주세요.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT = """{
            "code": "SD-0007",
            "message": "현재 모집된 인원이 수정하려는 모집 정원보다 많습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_MUST_BE_CLOSED_TO_APPROVE = """{
            "code": "SD-0008",
            "message": "모집이 완료된 스터디만 승인 할 수 있습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CANNOT_MODIFY_AFTER_DETERMINED = """{
            "code": "SD-0009",
            "message": "스터디 정보 수정은 결재되기 이전에만 가능합니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CAPACITY_TOO_BIG = """{
            "code": "SD-0010",
            "message": "스터티의 최대 모집 정원이 너무 많습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_TAG_COUNT_EXCEEDED = """{
            "code": "SD-0011",
            "message": "스터디 태그는 최대 5개까지 가질수 있습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CANT_DELETE_STATUS_DETERMINE = """{
            "code": "SD-0012",
            "message": "결재 상신된 스터디는 삭제할 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_ALREADY_FINISH_TO_RECRUIT = """{
            "code": "SD-0013",
            "message": "해당 스터디의 모집기한이 이미 지났습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_MIN_MEMBER_REQUIRED = """{
            "code": "SD-0014",
            "message": "스터디장은 탈퇴 할 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_CANNOT_MODIFY_AFTER_DEADLINE = """{
            "code": "SD-0015",
            "message": "모집 마감 상태에서 수정할 수 없는 항목입니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_NOT_FOUND = """{
            "code": "SD-0015",
            "message": "해당 스터디를 찾을수 없습니다.",
            "status": 404,
            "errors": []
        }"""
        const val MAX_STUDY_EXCEEDED = """{
            "code": "SD-0016",
            "message": "스터디는 최대 2개까지 가입 가능합니다.",
            "status": 400,
            "errors": []
        }"""
        const val NOT_STUDY_LEADER = """{
            "code": "SD-0017",
            "message": "스터디장만 사용할 수 있습니다.",
            "status": 403,
            "errors": []
        }"""
    }

    object Recruitment {
        const val RECRUITMENT_CANCEL_NOT_ALLOWED_STUDY_NOT_PENDING = """{
            "code": "SR-0002",
            "message": "스터디 상태가 PENDING 이 아닌 경우 신청을 취소할 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val RECRUITMENT_INVALID_STATUS_CHANGE = """{
            "code": "SR-0003",
            "message": "유효하지 않은 상태 변경입니다.",
            "status": 400,
            "errors": []
        }"""
        const val NOT_RECRUITMENT_OWNER = """{
            "code": "SR-0005",
            "message": "본인이 작성한 신청만 취소할 수 있습니다.",
            "status": 403,
            "errors": []
        }"""
        const val LEADER_CANNOT_LEAVE = """{
            "code": "SR-0006",
            "message": "스터디장은 탈퇴할 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val TRACK_MISMATCH = """{
            "code": "SR-0007",
            "message": "신청자의 과정과 스터디의 과정이 일치하지 않습니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_NOT_RECRUITING = """{
            "code": "SR-0008",
            "message": "해당 스터디는 모집중이 아닙니다.",
            "status": 400,
            "errors": []
        }"""
        const val ALREADY_APPLIED = """{
            "code": "SR-0009",
            "message": "이미 신청하거나 승인된 스터디입니다.",
            "status": 400,
            "errors": []
        }"""
        const val RECRUITMENT_NOT_FOUND = """{
            "code": "SR-0010",
            "message": "해당 스터디에 대한 신청기록을 찾을 수 없습니다.",
            "status": 404,
            "errors": []
        }"""
        const val STUDY_MONTH_IS_NOT_CURRENT_MONTH = """{
            "code": "SR-0011",
            "message": "신청한 차수는 현재 진행되는 차수에 해당하지 않습니다.",
            "status": 400,
            "errors": []
        }"""
    }

    object Schedule {
        const val STUDY_CANT_START_AFTER_END_DATE = """{
            "code": "SS-0001",
            "message": "모집 시작은 모집 종료보다 빨라야 합니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE = """{
            "code": "SS-0002",
            "message": "모집 종료는 스터디 종료보다 빨라야 합니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_MONTH_ILLEGAL_RANGE = """{
            "code": "SS-0003",
            "message": "유효하지 않은 차수입니다. 1 ~ 6차 사이여야 합니다.",
            "status": 400,
            "errors": []
        }"""
        const val STUDY_SCHEDULE_IS_NOT_IN_TRACK = """{
            "code": "SS-0004",
            "message": "해당 차수 일정은 현재 과정에 존재하지 않습니다.",
            "status": 400,
            "errors": []
        }"""
        const val SCHEDULE_OVERLAP_WITH_NEXT = """{
            "code": "SS-0005",
            "message": "현재 차수의 스터디 종료일은 다음 차수의 모집 시작일 보다 늦을 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val DUPLICATE_SCHEDULE_MONTH = """{
            "code": "SS-0006",
            "message": "해당 트랙에 이미 존재하는 차수 입니다.",
            "status": 400,
            "errors": []
        }"""
        const val SCHEDULE_NOT_FOUND = """{
            "code": "SS-0007",
            "message": "해당 일정이 존재하지 않습니다.",
            "status": 404,
            "errors": []
        }"""
        const val CANNOT_DELETE_SCHEDULE_WITH_STUDIES = """{
            "code": "SS-0008",
            "message": "해당 일정을 참조하는 스터디가 있어 삭제할 수 없습니다.",
            "status": 409,
            "errors": []
        }"""
        const val SCHEDULE_OVERLAP_WITH_PREVIOUS = """{
            "code": "SS-0009",
            "message": "이전 차수의 스터디 종료일은 현재 차수의 스터디 모집 시작일 보다 늦을 수 없습니다.",
            "status": 400,
            "errors": []
        }"""
        const val NO_CURRENT_SCHEDULE = """{
            "code": "SS-0010",
            "message": "현재 스터디 신청 과정이 아닙니다.",
            "status": 400,
            "errors": []
        }"""
    }

    object StudyTrack {
        const val TRACK_IS_NOT_ENROLLED = """{
            "code": "SDT-0001",
            "message": "해당 과정을 수강중인 상태가 아닙니다.",
            "status": 400,
            "errors": []
        }"""
        const val TRACK_NOT_FOUND = """{
            "code": "SDT-0002",
            "message": "해당 과정을 찾을 수 없습니다.",
            "status": 404,
            "errors": []
        }"""
    }

    object Tag {
         const val TAG_LENGTH_INVALID_RANGE = """{
            "code": "TG-0001",
            "message": "태그의 길이가 양식에 맞지 않습니다.",
            "status": 400,
            "errors": []
        }"""
        const val TAG_FORMAT_INVALID = """{
            "code": "TG-0002",
            "message": "태그의 입력 양식이 올바르지 않습니다.",
            "status": 400,
            "errors": []
        }"""
        const val TAG_CREATION_FAIL = """{
            "code": "TG-0001",
            "message": "태그 생성 및 조회에 실패했습니다.",
            "status": 409,
            "errors": []
        }"""
    }
}
