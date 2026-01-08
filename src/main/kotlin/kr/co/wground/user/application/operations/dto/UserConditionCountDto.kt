package kr.co.wground.user.application.operations.dto

data class UserConditionCountDto(
    val totalCount: Long,
    val signupSummary: SignupCount? = null,
    val roleSummary: RoleCount? = null,
    val statusSummary: StatusCount? = null,
    val academicSummary: AcademicCount? = null
)

data class SignupCount(
    val pending: Long,
    val accepted: Long,
    val rejected: Long)

data class RoleCount(
    val member: Long,
    val instructor: Long,
    val admin: Long)

data class StatusCount(
    val active: Long,
    val inactive: Long)

data class AcademicCount(
    val graduated: Long,
    val undergraduate: Long)
