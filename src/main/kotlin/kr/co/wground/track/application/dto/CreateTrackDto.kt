package kr.co.wground.track.application.dto

import kr.co.wground.track.domain.Track
import java.time.LocalDate

data class CreateTrackDto(
    val trackName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
){
    fun toEntity(): Track {
        return Track(
            trackName = this.trackName,
            startDate = this.startDate,
            endDate = this.endDate,
        )
    }
}
