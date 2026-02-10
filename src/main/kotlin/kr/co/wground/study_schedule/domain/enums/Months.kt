package kr.co.wground.study_schedule.domain.enums

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.study_schedule.domain.exception.StudyScheduleDomainErrorCode

enum class Months(val month: String) {
    FIRST("1"),
    SECOND("2"),
    THIRD("3"),
    FOURTH("4"),
    FIFTH("5"),
    SIXTH("6"),
    ;

    companion object {
                 fun from(value: String): Months {
                         return entries.find { it.name == value }
                             ?: throw BusinessException(StudyScheduleDomainErrorCode.STUDY_MONTH_ILLEGAL_RANGE)
                     }
             }
}