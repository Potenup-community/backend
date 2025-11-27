package kr.co.wground.track.application.dto

import kr.co.wground.track.domain.Track

fun CreateTrackDto.toEntity(): Track {
    return Track(
        trackName = this.trackName,
        startDate = this.startDate,
        endDate = this.endDate,
    )
}
