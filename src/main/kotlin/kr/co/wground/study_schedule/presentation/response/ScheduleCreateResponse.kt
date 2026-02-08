package kr.co.wground.study_schedule.presentation.response

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months

data class ScheduleCreateResponse(
    val id: Long,
    val trackId: TrackId,
    val months: Months
) {
    companion object {
        fun of(id: Long, trackId: TrackId, month: Months): ScheduleCreateResponse {
            return ScheduleCreateResponse(
                id = id,
                trackId = trackId,
                months = month
            )
        }
    }

}
