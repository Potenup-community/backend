package kr.co.wground.dashboard.presentation.response

import kr.co.wground.dashboard.application.dto.DashboardOverviewDto

data class DashboardOverviewResponse(
    val totalPostCount: Long,
    val totalUsers: Long,
)

fun DashboardOverviewDto.toResponse() = DashboardOverviewResponse(totalPostCount, totalUsers)
