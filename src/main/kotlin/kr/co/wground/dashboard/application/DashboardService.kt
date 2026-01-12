package kr.co.wground.dashboard.application

import kr.co.wground.dashboard.application.dto.DashboardOverviewDto
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserCommandRepository
import org.springframework.stereotype.Service

@Service
class DashboardService(
    private val userRepository: UserCommandRepository,
    private val postRepository: PostRepository
) {
    fun getDashboardOverview(): DashboardOverviewDto {
        val userCount = userRepository.countByStatus(UserStatus.ACTIVE)
        val postCount = postRepository.count()

        return DashboardOverviewDto(postCount, userCount)
    }
}
