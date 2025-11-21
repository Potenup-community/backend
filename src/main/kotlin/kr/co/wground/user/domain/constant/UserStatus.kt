package kr.co.wground.user.domain.constant

enum class UserStatus {
    ADMIN,
    GRADUATED,
    ENROLLED,
    EXPIRED,
    BLOCKED;

    companion object{
        fun from(status: String): UserStatus {
            return when(status) {
                ADMIN.name -> ADMIN
                ENROLLED.name -> ENROLLED
                GRADUATED.name -> GRADUATED
                BLOCKED.name -> BLOCKED
                else -> EXPIRED
            }
        }
    }
}