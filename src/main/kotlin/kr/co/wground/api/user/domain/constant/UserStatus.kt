package kr.co.wground.api.user.domain.constant

enum class UserStatus {
    ADMIN,
    GRADUATED,
    ENROLLED,
    EXPIRED,
    BLOCKED;

    companion object{
        fun from(status: String): UserStatus {
            return when(status) {
                UserSignupStatus.ACCEPTED_ADMIN.name -> ADMIN
                UserSignupStatus.ACCEPTED_ENROLLED.name -> ENROLLED
                UserSignupStatus.ACCEPTED_GRADUATED.name -> GRADUATED
                UserSignupStatus.REJECTED.name -> BLOCKED
                else -> EXPIRED
            }
        }
    }
}