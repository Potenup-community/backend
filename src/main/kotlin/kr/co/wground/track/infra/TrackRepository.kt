package kr.co.wground.track.infra

import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TrackRepository : JpaRepository<Track, Long>  {
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Track t 
        SET t.trackStatus = :graduatedStatus 
        WHERE t.trackStatus = :enrolledStatus 
          AND t.endDate < :now
    """)
    fun expireTracks(
        @Param("now") now: LocalDate,
        @Param("enrolledStatus") enrolledStatus: TrackStatus,
        @Param("graduatedStatus") graduatedStatus: TrackStatus
    ): Int
}
