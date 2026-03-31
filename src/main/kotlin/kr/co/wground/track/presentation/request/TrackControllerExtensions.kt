package kr.co.wground.track.presentation.request

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.UpdateTrackDto

fun CreateTrackRequest.toCreateTrackDto(): CreateTrackDto {
    return CreateTrackDto(
        trackType = this.trackType,
        cardinal = this.cardinal,
        startDate = this.startDate,
        endDate = this.endDate
    )
}

fun UpdateTrackRequest.toUpdateTrackDto(trackId: TrackId): UpdateTrackDto {
    return UpdateTrackDto(
        trackId = trackId,
        trackType = this.trackType,
        cardinal = this.cardinal,
        startDate = this.startDate,
        endDate = this.endDate
    )
}
