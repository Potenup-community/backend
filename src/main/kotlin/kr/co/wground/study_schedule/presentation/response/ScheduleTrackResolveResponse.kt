package kr.co.wground.study_schedule.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "스터디 일정 등록용 과정 식별자 조회 응답")
data class ScheduleTrackResolveResponse(
    @field:Schema(description = "과정 유형", example = "BE")
    val trackType: TrackType,
    @field:Schema(description = "기수", example = "4")
    val cardinal: Int,
    @field:Schema(description = "선택된 과정 ID", example = "12")
    val trackId: TrackId,
)
