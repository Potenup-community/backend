package kr.co.wground.track.application

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.UpdateTrackDto
import kr.co.wground.track.presentation.response.TrackQueryResponse

interface TrackService {
    fun createTrack(createTrack: CreateTrackDto): List<TrackQueryResponse>
    fun updateTrack(updateTrack: UpdateTrackDto)
    fun deleteTrack(trackId: TrackId)
    fun getAllTrackResponses(): List<TrackQueryResponse>
}
