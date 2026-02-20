package kr.co.wground.gallery.presentation.response

import kr.co.wground.gallery.application.usecase.result.ProjectSummaryResult
import kr.co.wground.global.common.ProjectId
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class ProjectSummaryPageResponse(
    val content: List<ProjectSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: Page<ProjectSummaryResult>): ProjectSummaryPageResponse =
            ProjectSummaryPageResponse(
                content = page.content.map { ProjectSummaryResponse.from(it) },
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext(),
            )
    }
}

data class ProjectSummaryResponse(
    val projectId: ProjectId,
    val title: String,
    val thumbnailImageUrl: String,
    val trackNames: List<String>,
    val techStacks: List<String>,
    val memberCount: Long,
    val viewCount: Int,
    val reactionCount: Int,
    val reactedByMe: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: ProjectSummaryResult): ProjectSummaryResponse =
            ProjectSummaryResponse(
                projectId = result.projectId,
                title = result.title,
                thumbnailImageUrl = result.thumbnailImageUrl,
                trackNames = result.trackNames,
                techStacks = result.techStacks,
                memberCount = result.memberCount,
                viewCount = result.viewCount,
                reactionCount = result.reactionCount,
                reactedByMe = result.reactedByMe,
                createdAt = result.createdAt,
            )
    }
}
