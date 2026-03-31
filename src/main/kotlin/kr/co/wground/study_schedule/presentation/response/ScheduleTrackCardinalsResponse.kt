package kr.co.wground.study_schedule.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "스터디 일정 등록용 과정 기수 목록 응답")
data class ScheduleTrackCardinalsResponse(
    @field:Schema(description = "과정 유형", example = "BE")
    val trackType: TrackType,
    @field:Schema(description = "현재 ENROLLED 상태로 등록된 기수 목록", example = "[3,4]")
    val cardinals: List<Int>,
)
