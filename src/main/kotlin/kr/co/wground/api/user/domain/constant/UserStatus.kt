package kr.co.wground.api.user.domain.constant

enum class UserStatus {
    ADMIN,
    GRADUATED,
    ENROLLED,
    EXPIRED,
    BLOCKED;

    companion object{
        fun from(status: UserSignupStatus): UserStatus {
            return when(status) {
                UserSignupStatus.ACCEPTED_ADMIN -> ADMIN
                UserSignupStatus.ACCEPTED_ENROLLED -> ENROLLED
                UserSignupStatus.ACCEPTED_GRADUATED -> GRADUATED
                UserSignupStatus.REJECTED -> EXPIRED
                else -> BLOCKED
            }
        }
    }
}