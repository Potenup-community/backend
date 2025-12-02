package kr.co.wground.global.scheduler.track

import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ScheduleStartupLoader(
    private val trackRepository: TrackRepository,
    private val trackSchedulerManager: TrackSchedulerManager,
    @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private val batchSize: Int
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional(readOnly = true)
    fun loadSchedulerTask(event: ApplicationReadyEvent) {
        var pageNumber = 0
        while (true) {
            val pageRequest = PageRequest.of(pageNumber, batchSize)
            val tracks = trackRepository.findAllByTrackStatus(TrackStatus.ENROLLED, pageRequest)

            if (tracks.isEmpty()) break

            tracks.forEach { track ->
                trackSchedulerManager.registerTask(track.trackId, track.endDate)
            }
            pageNumber++
        }
    }
}