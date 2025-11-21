package kr.co.wground.user.domain.constant

enum class UserRole {
    ADMIN,
    MEMBER,
    INSTRUCTOR,
    EXPIRED,
    BLOCKED;

    companion object {
        fun from(status: String): UserRole {
            return when (status) {
                ADMIN.name -> ADMIN
                MEMBER.name -> MEMBER
                INSTRUCTOR.name -> INSTRUCTOR
                BLOCKED.name -> BLOCKED
                else -> EXPIRED
            }
        }
    }
}