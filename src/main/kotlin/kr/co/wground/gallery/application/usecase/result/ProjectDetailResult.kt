package kr.co.wground.gallery.application.usecase.result

import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

data class ProjectDetailResult(
    val projectId: ProjectId,
    val title: String,
    val description: String,
    val githubUrl: String,
    val deployUrl: String?,
    val thumbnailImageUrl: String,
    val techStacks: List<String>,
    val members: List<MemberInfo>,
    val viewCount: Int,
    val reactionCount: Int,
    val reactedByMe: Boolean,
    val author: AuthorInfo,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
) {
    data class MemberInfo(
        val userId: UserId,
        val name: String,
        val profileImageUrl: String,
        val trackName: String,
        val position: Position,
    )

    data class AuthorInfo(
        val userId: UserId,
        val name: String,
    )
}
