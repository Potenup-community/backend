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

    @Column(nullable = false)
    var lastAttendanceDate: LocalDate,

    @Column(nullable = false)
    var streakCount: Int,

    @Column(nullable = false)
    var maxStreakCount: Int,
) {
    companion object {
        private const val ONE_DAY = 1
        fun create(userId: UserId, today: LocalDate): Attendance {
            return Attendance(
                userId = userId,
                lastAttendanceDate = today,
                streakCount = 1,
                maxStreakCount = 1
            )
        }
    }

    fun checkIn(today: LocalDate): CheckInResult {
        validateAttendanceDate(today)

        if (today.isEqual(this.lastAttendanceDate)) return CheckInResult.AlreadyCheckedIn

        this.streakCount = when {
            lastAttendanceDate.plusDays(ONE_DAY.toLong()).isEqual(today) -> streakCount + ONE_DAY
            else -> ONE_DAY
        }

        this.maxStreakCount = maxOf(this.maxStreakCount, this.streakCount)
        this.lastAttendanceDate = today

        return CheckInResult.CheckedIn
    }

    private fun validateAttendanceDate(today: LocalDate) {
        if (today < this.lastAttendanceDate) {
            throw BusinessException(AttendanceErrorCode.ATTENDANCE_DAY_CANT_BEFORE_TODAY)
        }
    }
}


