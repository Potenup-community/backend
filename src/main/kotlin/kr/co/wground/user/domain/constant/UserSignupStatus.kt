package kr.co.wground.user.domain.constant

enum class UserSignupStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    companion object{
        fun isAccepted(status: UserSignupStatus): Boolean{
            return status == ACCEPTED
        }
    }
}
