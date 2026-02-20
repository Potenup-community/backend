package kr.co.wground.gallery.application.usecase.result

import kr.co.wground.global.common.ProjectId
import java.time.LocalDateTime

data class ProjectSummaryResult(
    val projectId: ProjectId,
    val title: String,
    val thumbnailImageUrl: String,
    val trackNames: List<String>,
    val techStacks: List<String>,
    val memberCount: Long,
    val viewCount: Int,
    val createdAt: LocalDateTime,
)
