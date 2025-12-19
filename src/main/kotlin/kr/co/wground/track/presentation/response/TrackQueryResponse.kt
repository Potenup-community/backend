package kr.co.wground.track.presentation.response

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import java.time.LocalDate

data class TrackQueryResponse private constructor(
    val trackId: Long,
    val trackName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val trackStatus: TrackStatus
) {
    companion object {
        fun fromEntity(entity: Track): TrackQueryResponse {
            return TrackQueryResponse(
                trackId = entity.trackId,
                trackName = entity.trackName,
                startDate = entity.startDate,
                endDate = entity.endDate,
                trackStatus = entity.trackStatus
            )
        }
    }
}
