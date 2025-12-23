package kr.co.wground.dashboard.application

import kr.co.wground.dashboard.application.dto.DashboardOverviewDto
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.user.infra.UserRepository
import org.springframework.stereotype.Service

@Service
class DashboardService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) {
    fun getDashboardOverview(): DashboardOverviewDto {
        val userCount = userRepository.count()
        val postCount = postRepository.count()

        return DashboardOverviewDto(postCount, userCount)
    }
}
