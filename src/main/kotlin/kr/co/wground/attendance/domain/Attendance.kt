package kr.co.wground.attendance.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDate
import kr.co.wground.attendance.exception.AttendanceErrorCode
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId

@Entity
class Attendance protected constructor(
    @Id
    val userId: UserId,

    var lastAttendanceDate: LocalDate? = null,

    @Column(nullable = false)
    var streakCount: Int,

    @Column(nullable = false)
    var maxStreakCount: Int,
) {
    companion object {
        private const val ONE_DAY = 1
        private const val INITIAL_STREAK = 0

        fun create(userId: UserId): Attendance =
            Attendance(
                userId = userId,
                lastAttendanceDate = null,
                streakCount = INITIAL_STREAK,
                maxStreakCount = INITIAL_STREAK
            )
    }

    fun checkIn(today: LocalDate): CheckInResult {
        lastAttendanceDate?.let { last ->
            validateAttendanceDate(today, last)
            if (today.isEqual(last)) return CheckInResult.AlreadyCheckedIn
        }

        val lastOrYesterday = lastAttendanceDate ?: today.minusDays(ONE_DAY.toLong())
        val isConsecutive = lastOrYesterday.plusDays(ONE_DAY.toLong()).isEqual(today)

        streakCount = if (isConsecutive) streakCount + ONE_DAY else ONE_DAY
        maxStreakCount = maxOf(maxStreakCount, streakCount)
        lastAttendanceDate = today

        return CheckInResult.CheckedIn
    }

    private fun validateAttendanceDate(today: LocalDate, last: LocalDate) {
        if (today.isBefore(last)) {
            throw BusinessException(AttendanceErrorCode.ATTENDANCE_DAY_CANT_BEFORE_TODAY)
        }
    }
}


