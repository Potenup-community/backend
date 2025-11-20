package kr.co.wground.api.user.domain.constant

enum class UserSignupStatus {
    PENDING,
    ACCEPTED_ADMIN,
    ACCEPTED_GRADUATED,
    ACCEPTED_ENROLLED,
    GUEST,
    REJECTED;

    fun getUserSignupStatus(): String {
        return this.name
    }
}