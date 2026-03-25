package kr.co.wground.attendance.application.command.usecase

import kr.co.wground.global.common.UserId

interface AttendanceCheckInUseCase {
    fun checkAttendance(userId: UserId)
}