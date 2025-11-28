package kr.co.wground.global.scheduler.track

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TrackSchedulerManagerTest {

    @Mock
    lateinit var trackTaskExecutor: TrackTaskExecutor

    private lateinit var taskScheduler: TestTaskScheduler
    private lateinit var schedulerManager: TrackSchedulerManager

    @BeforeEach
    fun setup() {
        taskScheduler = TestTaskScheduler()
        schedulerManager = TrackSchedulerManager(trackTaskExecutor, taskScheduler)
    }

    @Test
    @DisplayName("종료일이 지난 트랙은 즉시 만료 처리하고 스케줄링하지 않는다")
    fun registerTask_shouldExecuteImmediatelyForExpiredTrack() {
        val pastEndDate = LocalDate.now().minusDays(1)

        schedulerManager.registerTask(1L, pastEndDate)

        verify(trackTaskExecutor, times(1)).executeExpire(1L)
        assertTrue(taskScheduler.scheduledTasks.isEmpty())
        assertEquals(0, schedulerManager.tasksSize())
    }

    @Test
    @DisplayName("향후 종료 트랙은 스케줄링되고 실행 후 작업이 정리된다")
    fun registerTask_shouldScheduleFutureTrackAndCleanupAfterExecution() {
        val futureEndDate = LocalDate.now().plusDays(2)

        schedulerManager.registerTask(2L, futureEndDate)
        assertEquals(1, taskScheduler.scheduledTasks.size)

        taskScheduler.runAll()

        verify(trackTaskExecutor, times(1)).executeExpire(2L)
        assertEquals(0, schedulerManager.tasksSize())
    }

    private fun TrackSchedulerManager.tasksSize(): Int {
        val field = TrackSchedulerManager::class.java.getDeclaredField("tasks")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return (field.get(this) as Map<*, *>).size
    }

    private class TestTaskScheduler : TaskScheduler {
        val scheduledTasks = mutableListOf<Runnable>()

        override fun schedule(task: Runnable, startTime: Instant): ScheduledFuture<*> {
            scheduledTasks += task
            return TestScheduledFuture(task, scheduledTasks)
        }

        fun runAll() {
            val tasks = scheduledTasks.toList()
            scheduledTasks.clear()
            tasks.forEach { it.run() }
        }

        override fun schedule(task: Runnable, startTime: Date): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        override fun schedule(task: Runnable, trigger: Trigger): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        override fun scheduleAtFixedRate(task: Runnable, period: Long): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        override fun scheduleAtFixedRate(task: Runnable, startTime: Date, period: Long): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        override fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        override fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> =
            throw UnsupportedOperationException()

        private class TestScheduledFuture(
            private val task: Runnable,
            private val container: MutableList<Runnable>
        ) : ScheduledFuture<Unit> {
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return container.remove(task)
            }

            override fun isCancelled(): Boolean = false

            override fun isDone(): Boolean = false

            override fun get(): Unit? = null

            override fun get(timeout: Long, unit: TimeUnit): Unit? = null

            override fun getDelay(unit: TimeUnit): Long = 0

            override fun compareTo(other: Delayed): Int = 0
        }
    }
}
