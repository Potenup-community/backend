package kr.co.wground.track.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.UpdateTrackDto
import kr.co.wground.track.application.dto.toEntity
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class TrackServiceImpl(
    val trackRepository: TrackRepository,
) : TrackService {
    override fun createTrack(createTrack: CreateTrackDto) {
        trackRepository.save(createTrack.toEntity())
    }

    override fun updateTrack(updateTrack: UpdateTrackDto) {
        val track = trackRepository.findByIdOrNull(updateTrack.trackId)
            ?: throw BusinessException(TrackServiceErrorCode.TRACK_NOT_FOUND)

        track.updateTrack(
            trackName = updateTrack.trackName,
            startDate = updateTrack.startDate,
            endDate = updateTrack.endDate
        )
    }

    override fun deleteTrack(trackId: TrackId) {
        trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(TrackServiceErrorCode.TRACK_NOT_FOUND)

        trackRepository.deleteById(trackId)
    }

    @Transactional
    override fun expireOverdueTracks(now: LocalDate) {
        trackRepository.expireTracks(
            now = now,
            enrolledStatus = TrackStatus.ENROLLED,
            graduatedStatus = TrackStatus.GRADUATED
        )
    }
}
