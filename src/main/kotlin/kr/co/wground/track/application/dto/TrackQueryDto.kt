package kr.co.wground.track.application.dto

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import java.time.LocalDate

data class TrackQueryDto private constructor(
    val trackId: Long,
    val trackName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val trackStatus: TrackStatus
) {
    companion object {
        fun Track.toTrackQueryDto(): TrackQueryDto {
            return TrackQueryDto(
                trackId = this.trackId,
                trackName = this.trackName,
                startDate = this.startDate,
                endDate = this.endDate,
                trackStatus = this.trackStatus
            )
        }
    }
}