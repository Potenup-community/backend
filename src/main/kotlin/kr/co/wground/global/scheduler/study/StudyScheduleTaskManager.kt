package kr.co.wground.global.scheduler.study

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Component
class StudyScheduleTaskManager(
    private val studyScheduleNotificationTasks: StudyScheduleNotificationTasks,
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

        // 스터디 모집 시작 알림 이벤트 등록
        if (recruitStart.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleNotificationTasks.publishStudyRecruitStartedEvent(scheduleId) },
                recruitStart.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        // 스터디 모집 종료 예정 알림 이벤트 등록
        val recruitEndNotifyTime = recruitEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS)
        if (recruitEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleNotificationTasks.publishStudyRecruitEndedSoonEvent(scheduleId) },
                recruitEndNotifyTime.atZone(ZoneId.systemDefault()).toInstant()
            )
            newTasks.add(future)
        }

        // 스터디 종료 예정 알림 이벤트 등록
        val studyEndNotifyTime = studyEnd.minusDays(STUDY_ALERT_PREVIOUS_DAYS)
        if (studyEndNotifyTime.isAfter(now)) {
            val future = taskScheduler.schedule(
                { studyScheduleNotificationTasks.publishStudyEndedSoonEvent(scheduleId) },
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
