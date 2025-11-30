package kr.co.wground.global.scheduler.track

import kr.co.wground.track.application.event.TrackChangedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TrackEventListener(
    private val scheduleManager: TrackSchedulerManager
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleTrackChange(event: TrackChangedEvent) {
        when (event.type) {
            TrackChangedEvent.EventType.CREATED,
            TrackChangedEvent.EventType.UPDATED -> {
                scheduleManager.registerTask(event.trackId, event.endDate)
            }

            TrackChangedEvent.EventType.DELETED -> {
                scheduleManager.cancel(event.trackId)
            }
        }
    }
}