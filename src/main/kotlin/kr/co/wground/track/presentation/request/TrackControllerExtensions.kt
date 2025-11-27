package kr.co.wground.track.presentation.request

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.UpdateTrackDto

fun CreateTrackRequest.toCreateTrackDto(): CreateTrackDto {
    return CreateTrackDto(
        trackName = this.trackName,
        startDate = this.startDate,
        endDate = this.endDate
    )
}

fun UpdateTrackRequest.toUpdateTrackDto(trackId: TrackId): UpdateTrackDto {
    return UpdateTrackDto(
        trackId = trackId,
        trackName = this.trackName,
        startDate = this.startDate,
        endDate = this.endDate
    )
}
