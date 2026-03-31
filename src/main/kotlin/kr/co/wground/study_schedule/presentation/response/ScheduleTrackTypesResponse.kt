package kr.co.wground.study_schedule.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "스터디 일정 등록용 과정 유형 목록 응답")
data class ScheduleTrackTypesResponse(
    val trackTypes: List<TrackTypeItem>,
) {
    data class TrackTypeItem(
        @field:Schema(description = "과정 유형 코드", example = "BE")
        val trackType: TrackType,
        @field:Schema(description = "과정 유형 표시명", example = "BE")
        val label: String,
        @field:Schema(description = "기수 입력 필요 여부", example = "true")
        val requiresCardinal: Boolean,
    )

    companion object {
        fun from(trackTypes: List<TrackType>): ScheduleTrackTypesResponse {
            return ScheduleTrackTypesResponse(
                trackTypes = trackTypes.map {
                    TrackTypeItem(
                        trackType = it,
                        label = it.displayName,
                        requiresCardinal = true
                    )
                }
            )
        }
    }
}
