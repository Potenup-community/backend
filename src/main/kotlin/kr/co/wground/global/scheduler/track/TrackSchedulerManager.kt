package kr.co.wground.global.scheduler.track

import kr.co.wground.global.common.TrackId
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Component
class TrackSchedulerManager(
    private val trackTaskExecutor: TrackTaskExecutor,
    private val taskScheduler: TaskScheduler,
) {
    private val tasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    fun registerTask(trackId: TrackId, endDate: LocalDate) {
        cancel(trackId)

        val executeTime = endDate.plusDays(1).atStartOfDay()

        if (executeTime.isBefore(LocalDateTime.now())) {
            trackTaskExecutor.executeExpire(trackId)
            return
        }

        val future = taskScheduler.schedule(
            {
                try {
                    trackTaskExecutor.executeExpire(trackId)
                } finally {
                    tasks.remove(trackId)
                }
            },
            executeTime.atZone(ZoneId.systemDefault()).toInstant()
        )
        tasks[trackId] = future
    }

    fun cancel(trackId: Long) {
        tasks[trackId]?.let {
            it.cancel(false)
            tasks.remove(trackId)
        }
    }
}