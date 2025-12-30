package kr.co.wground.track.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.TrackQueryDto
import kr.co.wground.track.application.dto.TrackQueryDto.Companion.toTrackQueryDto
import kr.co.wground.track.application.dto.UpdateTrackDto
import kr.co.wground.track.application.event.TrackChangedEvent
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TrackServiceImpl(
    private val trackRepository: TrackRepository,
    private val eventPublisher: ApplicationEventPublisher
) : TrackService {
    companion object {
        private const val ADMIN_TRACK = 1L
    }
    override fun createTrack(createTrack: CreateTrackDto): List<TrackQueryDto> {
        val savedTrack = trackRepository.save(createTrack.toEntity())

        //스케줄러 만료 변환 날짜 등록
        eventPublisher.publishEvent(
            TrackChangedEvent(
                trackId = savedTrack.trackId, endDate = savedTrack.endDate, type = TrackChangedEvent.EventType.CREATED
            )
        )
        return getAllTrackResponses()
    }

    override fun updateTrack(updateTrack: UpdateTrackDto) {
        val track = findTrackById(updateTrack.trackId)

        track.updateTrack(
            trackName = updateTrack.trackName,
            startDate = updateTrack.startDate,
            endDate = updateTrack.endDate
        )
        //스케줄러 만료 변환 날짜 수정
        eventPublisher.publishEvent(
            TrackChangedEvent(
                trackId = track.trackId, endDate = track.endDate, type = TrackChangedEvent.EventType.UPDATED
            )
        )
    }

    override fun deleteTrack(trackId: TrackId) {
        val deletedTrack = findTrackById(trackId)

        trackRepository.delete(deletedTrack)

        //스케줄러 만료 변환 날짜 삭제
        eventPublisher.publishEvent(
            TrackChangedEvent(
                trackId = deletedTrack.trackId,
                endDate = deletedTrack.endDate,
                type = TrackChangedEvent.EventType.DELETED
            )
        )
    }

    private fun findTrackById(id: TrackId): Track {
        return trackRepository.findByIdOrNull(id)
            ?: throw BusinessException(TrackServiceErrorCode.TRACK_NOT_FOUND)
    }

    override fun getAllTrackResponses(): List<TrackQueryDto> {
        return trackRepository.findAllTracks().map{ it.toTrackQueryDto()}
    }

    override fun getTracksExceptAdmin(): List<TrackQueryDto> {
        return trackRepository.findAllByTrackIdNotOrderByEndDateDesc(ADMIN_TRACK).map{ it.toTrackQueryDto()}
    }
}
