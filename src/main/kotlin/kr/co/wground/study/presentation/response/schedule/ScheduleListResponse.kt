package kr.co.wground.study.presentation.response.schedule

import kr.co.wground.study.application.dto.StudyCreateScheduleDto

data class ScheduleListResponse(
    val content : List<StudyCreateScheduleDto>,
)
