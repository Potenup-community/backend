package kr.co.wground.track.infra

import kr.co.wground.track.application.TrackService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class TrackScheduler(
    private val trackService: TrackService
) {
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    fun autoExpireTracks() {
        val now = LocalDate.now()
        trackService.expireOverdueTracks(now)
    }
}