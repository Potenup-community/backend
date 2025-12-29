package kr.co.wground.track.application.dto

import kr.co.wground.global.common.TrackId
import java.time.LocalDate

data class UpdateTrackDto(
    val trackId: TrackId,
    val trackName: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
