package kr.co.wground.track.infra

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, Long> {
    fun findAllByTrackStatus(status: TrackStatus, pageRequest: Pageable): Page<Track>
    fun findAllByTrackIdNotOrderByEndDateDesc(trackId: Long): List<Track>

    @Query("""
    SELECT t FROM Track t 
    ORDER BY CASE WHEN t.trackId = 1 THEN 0 ELSE 1 END ASC, t.endDate DESC
    """)
    fun findAllByOrderByEndDateDesc(): List<Track>
}
