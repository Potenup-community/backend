package kr.co.wground.attendance.infra

import kr.co.wground.attendance.domain.Attendance
import org.springframework.data.jpa.repository.JpaRepository

interface AttendanceRepository: JpaRepository<Attendance, Long> {
}