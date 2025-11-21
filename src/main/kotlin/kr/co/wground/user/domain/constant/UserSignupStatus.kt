package kr.co.wground.user.domain.constant

enum class UserSignupStatus {
    PENDING,
    ACCEPTED,
    GUEST,
    REJECTED;

    fun getUserSignupStatus(): String {
        return this.name
    }
    companion object{
        fun from(status: String): UserSignupStatus {
            return when(status) {
                PENDING.name -> PENDING
                ACCEPTED.name -> ACCEPTED
                GUEST.name -> GUEST
                REJECTED.name -> REJECTED
                else -> PENDING
            }
        }
    }
}