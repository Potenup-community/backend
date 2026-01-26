package kr.co.wground.user.application.operations.dto

import kr.co.wground.user.application.operations.constant.COUNT_DEFAULT_VALUE

data class UserConditionCountDto(
    val totalCount: Long,
    val signupSummary: SignupCount,
    val roleSummary: RoleCount,
    val statusSummary: StatusCount,
    val academicSummary: AcademicCount
)

data class SignupCount(
    val isConditionMet: Boolean,
    val pending: Long,
    val accepted: Long,
    val rejected: Long
) {
    companion object {
        fun default(isConditionMet: Boolean = true) = SignupCount(
            isConditionMet = isConditionMet,
            pending = COUNT_DEFAULT_VALUE,
            accepted = COUNT_DEFAULT_VALUE,
            rejected = COUNT_DEFAULT_VALUE
        )

        fun empty() = default(isConditionMet = false)
    }
}

data class RoleCount(
    val isConditionMet: Boolean,
    val member: Long,
    val instructor: Long,
    val admin: Long
){
    companion object {
        fun default(isConditionMet: Boolean = true) = RoleCount(
            isConditionMet = isConditionMet,
            member = COUNT_DEFAULT_VALUE,
            instructor = COUNT_DEFAULT_VALUE,
            admin = COUNT_DEFAULT_VALUE
        )

        fun empty() = RoleCount.default(isConditionMet = false)
    }
}

data class StatusCount(
    val isConditionMet: Boolean,
    val active: Long,
    val inactive: Long
){
    companion object {
        fun default(isConditionMet: Boolean = true) = StatusCount(
            isConditionMet = isConditionMet,
            active = COUNT_DEFAULT_VALUE,
            inactive = COUNT_DEFAULT_VALUE
        )

        fun empty() = StatusCount.default(isConditionMet = false)
    }
}

data class AcademicCount(
    val isConditionMet: Boolean,
    val graduated: Long,
    val undergraduate: Long
){
    companion object {
        fun default(isConditionMet: Boolean = true) = AcademicCount(
            isConditionMet = isConditionMet,
            graduated = COUNT_DEFAULT_VALUE,
            undergraduate = COUNT_DEFAULT_VALUE
        )

        fun empty() = AcademicCount.default(isConditionMet = false)
    }
}
