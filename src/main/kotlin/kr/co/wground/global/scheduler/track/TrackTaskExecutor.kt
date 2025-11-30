package kr.co.wground.global.scheduler.track

import kr.co.wground.global.common.TrackId
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class TrackTaskExecutor(
    private val trackRepository: TrackRepository
) {
    @Transactional
    fun executeExpire(trackId: TrackId) {
        val track = trackRepository.findByIdOrNull(trackId) ?: return

        if (track.trackStatus == TrackStatus.ENROLLED) {
            track.refreshStatus(LocalDate.now())
        }
    }
}