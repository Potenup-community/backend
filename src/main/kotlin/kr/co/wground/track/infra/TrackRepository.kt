package kr.co.wground.track.infra

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.constant.TrackType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, Long>, CustomTrackRepository {
    fun findAllByTrackStatus(status: TrackStatus): List<Track>
    fun findAllByTrackStatus(status: TrackStatus, pageRequest: Pageable): Page<Track>
    fun findAllByTrackType(trackType: TrackType): List<Track>
    fun findAllByTrackStatusAndTrackType(status: TrackStatus, trackType: TrackType): List<Track>
    fun findFirstByTrackTypeAndCardinal(trackType: TrackType, cardinal: Int): Track?
    fun findFirstByTrackTypeAndCardinalAndTrackStatus(trackType: TrackType, cardinal: Int, trackStatus: TrackStatus): Track?
    fun findFirstByTrackType(trackType: TrackType): Track?
}
