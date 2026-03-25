package kr.co.wground.attendance.application.command

import com.github.benmanes.caffeine.cache.Cache
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kr.co.wground.attendance.application.command.usecase.AttendanceCheckInUseCase
import kr.co.wground.attendance.domain.Attendance
import kr.co.wground.attendance.domain.CheckInResult
import kr.co.wground.attendance.infra.AttendanceRepository
import kr.co.wground.common.event.AttendanceCheckedEvent
import kr.co.wground.global.common.UserId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val attendanceDailyCheckCache: Cache<Long, Boolean>,
    private val eventPublisher: ApplicationEventPublisher
): AttendanceCheckInUseCase {

    override fun checkAttendance(userId: UserId) {
        if (attendanceDailyCheckCache.getIfPresent(userId) != null) return

        val today = LocalDate.now()

        val attendance = attendanceRepository.findByIdOrNull(userId)
            ?: attendanceRepository.save(Attendance.create(userId))

        attendance.checkIn(today)
            .also { attendanceDailyCheckCache.put(userId, true) }
            .takeIf { it !is CheckInResult.AlreadyCheckedIn }
            ?: return

        eventPublisher.publishEvent(
            AttendanceCheckedEvent(
                userId = userId,
                streakCount = attendance.streakCount,
                attendanceDate = today
                    .format(DateTimeFormatter.BASIC_ISO_DATE)
                    .toLong()
            )
        )
    }
}