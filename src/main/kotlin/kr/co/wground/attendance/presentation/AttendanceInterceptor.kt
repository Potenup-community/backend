package kr.co.wground.attendance.presentation

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.attendance.application.command.usecase.AttendanceCheckInUseCase
import kr.co.wground.global.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AttendanceInterceptor(
    private val attendanceCheckInUseCase: AttendanceCheckInUseCase
): HandlerInterceptor {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        try {
            val userId = SecurityUtils.getCurrentUserId() ?: return true
            attendanceCheckInUseCase.checkAttendance(userId)
        } catch (e: Exception) {
            log.error("[Attendance] 출석 체크 실패: {}", e.message)
        }
        return true
    }
}