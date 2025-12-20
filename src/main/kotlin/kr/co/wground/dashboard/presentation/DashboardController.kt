package kr.co.wground.dashboard.presentation

import kr.co.wground.dashboard.application.DashboardService
import kr.co.wground.dashboard.presentation.response.DashboardOverviewResponse
import kr.co.wground.dashboard.presentation.response.toResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    @GetMapping("/overview")
    fun getDashboard(): DashboardOverviewResponse {
        return dashboardService.getDashboardOverview().toResponse()
    }
}
