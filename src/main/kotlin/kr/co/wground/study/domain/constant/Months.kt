package kr.co.wground.study.domain.constant

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

enum class Months(month: String) {
    FIRST("1차"),
    SECOND("2차"),
    THIRD("3차"),
    FOURTH("4차"),
    FIFTH("5차"),
    SIXTH("6차"),
    ;

    companion object {
                 fun from(value: String): Months {
                         return entries.find { it.name == value }
                             ?: throw BusinessException(StudyDomainErrorCode.STUDY_MONTH_ILLEGAL_RANGE)
                     }
             }
}