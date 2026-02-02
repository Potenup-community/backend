package kr.co.wground.global.scheduler.study

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Component
class StudySchedulerManager(
    private val studyTaskExecutor: StudyTaskExecutor,
    private val taskScheduler: TaskScheduler
) {
    private val tasks = ConcurrentHashMap<Long, MutableList<ScheduledFuture<*>>>()

    companion object {
        const val STUDY_ALERT_PREVIOUS_DAYS = 3L
    }
    fun registerSchedule(
        scheduleId: Long,
        recruitStart: LocalDateTime,
        recruitEnd: LocalDateTime,
        studyEnd: LocalDateTime
    ) {
        cancelSchedule(scheduleId)

        val newTasks = mutableListOf<ScheduledFuture<*>>()
        val now = LocalDateTime.now()

        if (recruitStart.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyTaskExecutor.executeRecruitStart(scheduleId) },
                recruitStart.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        val recruitEndNotifyTime = recruitEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS)// 3일 전 알림
        if (recruitEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyTaskExecutor.executeRecruitEnd(scheduleId) },
                recruitEndNotifyTime.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        val studyEndNotifyTime = studyEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS) // 3일 전 알림
        if (studyEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyTaskExecutor.executeStudyEnd(scheduleId) },
                studyEndNotifyTime.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        if (newTasks.isNotEmpty()) {
            tasks[scheduleId] = newTasks
        }
    }

    fun cancelSchedule(scheduleId: Long) {
        tasks[scheduleId]?.forEach { it.cancel(false) }
        tasks.remove(scheduleId)
    }
}
