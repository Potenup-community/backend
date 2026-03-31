package kr.co.wground.track.application.dto

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackType
import java.time.LocalDate

data class CreateTrackDto(
    val trackType: TrackType,
    val cardinal: Int? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
){
    fun toEntity(): Track {
        return Track(
            trackType = this.trackType,
            cardinal = this.cardinal,
            startDate = this.startDate,
            endDate = this.endDate,
        )
    }
}
