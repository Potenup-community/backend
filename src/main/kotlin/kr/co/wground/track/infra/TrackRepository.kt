package kr.co.wground.track.infra

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, Long>  {
    fun findAllByTrackStatus(status: TrackStatus, pageRequest: Pageable): Page<Track>
    fun findAllByCreatedAtDesc(): List<Track>
}
