package kr.co.wground.session.domain

enum class SessionStatus {
    ACTIVE,
    REVOKED,
    EXPIRED;

    fun isActive(): Boolean = this == ACTIVE
}
