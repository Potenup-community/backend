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
        fun Track.toTrackQueryResponse(): TrackQueryResponse {
            return TrackQueryResponse(
                trackId = this.trackId,
                trackName = this.trackName,
                startDate = this.startDate,
                endDate = this.endDate,
                trackStatus = this.trackStatus
            )
        }
    }
}
