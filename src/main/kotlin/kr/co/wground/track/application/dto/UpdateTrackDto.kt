package kr.co.wground.track.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.domain.constant.TrackType
import java.time.LocalDate

data class UpdateTrackDto(
    val trackId: TrackId,
    val trackType: TrackType? = null,
    val cardinal: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
