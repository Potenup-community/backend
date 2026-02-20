package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class AttendanceCheckedEvent(
    val userId: UserId,
    val streakCount: Int,
    val attendanceDate: Long,
)