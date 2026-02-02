package kr.co.wground.study.presentation.response.schedule

import kr.co.wground.study.application.dto.QueryStudyScheduleDto

data class ScheduleListResponse(
    val content : List<QueryStudyScheduleDto>,
)
