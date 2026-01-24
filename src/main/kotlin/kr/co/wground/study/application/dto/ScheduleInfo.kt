package kr.co.wground.study.application.dto

import java.time.LocalDateTime
import kr.co.wground.study.domain.constant.Months

data class ScheduleInfo(
    val month: Months,
    val recruitStart: LocalDateTime,
    val studyEnd: LocalDateTime
)