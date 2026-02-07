package kr.co.wground.study_schedule.presentation.response

import kr.co.wground.study_schedule.application.dto.QueryStudyScheduleDto

data class ScheduleListResponse(
    val content : List<QueryStudyScheduleDto>,
)
