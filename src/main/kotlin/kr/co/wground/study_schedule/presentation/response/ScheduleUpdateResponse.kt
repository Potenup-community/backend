package kr.co.wground.study_schedule.presentation.response

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months

data class ScheduleUpdateResponse(
    val id: Long,
    val trackId: TrackId,
    val months: Months
){
    companion object {
        fun of(id: Long, trackId: TrackId, month: Months): ScheduleUpdateResponse {
            return ScheduleUpdateResponse(
                id = id,
                trackId = trackId,
                months = month
            )
        }
    }
}
