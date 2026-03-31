package kr.co.wground.session.application.dto

/**
 * Presentation → Application 경계의 디바이스 컨텍스트 커맨드 객체.
 * 도메인 Value Object가 아닌 Application DTO다.
 */
data class DeviceContext(
    val deviceId: String,
    val deviceName: String?,
    val userAgent: String?,
    val ipAddress: String?,
)
