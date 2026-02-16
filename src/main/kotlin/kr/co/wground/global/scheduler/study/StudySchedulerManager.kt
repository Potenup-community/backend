package kr.co.wground.global.scheduler.study

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Component
class StudySchedulerManager(
    private val studyScheduleEventPublisher: StudyScheduleEventPublisher,
    private val taskScheduler: TaskScheduler
) {

    private val tasks = ConcurrentHashMap<Long, MutableList<ScheduledFuture<*>>>()

    companion object {
        const val STUDY_ALERT_PREVIOUS_DAYS = 3L
    }

    fun addTask(
        scheduleId: Long,
        recruitStart: LocalDateTime,
        recruitEnd: LocalDateTime,
        studyEnd: LocalDateTime
    ) {

        // scheduleId 에 해당하는 태스크 건이 이미 스케쥴링 되어 있는 경우 제거
        removeTask(scheduleId)

        val newTasks = mutableListOf<ScheduledFuture<*>>()
        val now = LocalDateTime.now()

        if (recruitStart.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleEventPublisher.publishStudyRecruitStartedEvent(scheduleId) },
                recruitStart.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        val recruitEndNotifyTime = recruitEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS)// 3일 전 알림
        if (recruitEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleEventPublisher.publishStudyRecruitEndedEvent(scheduleId) },
                recruitEndNotifyTime.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        val studyEndNotifyTime = studyEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS) // 3일 전 알림
        if (studyEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleEventPublisher.publishStudyEndedEvent(scheduleId) },
                studyEndNotifyTime.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        if (newTasks.isNotEmpty()) {
            tasks[scheduleId] = newTasks
        }
    }

    fun removeTask(scheduleId: Long) {

        tasks[scheduleId]?.forEach { it.cancel(false) }
        tasks.remove(scheduleId)
    }
}
