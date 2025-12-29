package kr.co.wground.track.application

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.TrackQueryDto
import kr.co.wground.track.application.dto.UpdateTrackDto

interface TrackService {
    fun createTrack(createTrack: CreateTrackDto): List<TrackQueryDto>
    fun updateTrack(updateTrack: UpdateTrackDto)
    fun deleteTrack(trackId: TrackId)
    fun getAllTrackResponses(): List<TrackQueryDto>
}
