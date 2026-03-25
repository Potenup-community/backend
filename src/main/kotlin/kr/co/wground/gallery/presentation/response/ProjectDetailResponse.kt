package kr.co.wground.gallery.presentation.response

import kr.co.wground.gallery.application.usecase.result.ProjectDetailResult
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

data class ProjectDetailResponse(
    val projectId: ProjectId,
    val title: String,
    val description: String,
    val githubUrl: String,
    val deployUrl: String?,
    val thumbnailImageUrl: String,
    val techStacks: List<String>,
    val members: List<MemberResponse>,
    val viewCount: Int,
    val reactionCount: Int,
    val reactedByMe: Boolean,
    val author: AuthorResponse,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
) {
    data class MemberResponse(
        val userId: UserId,
        val name: String,
        val profileImageUrl: String,
        val trackName: String,
        val position: Position,
    )

    data class AuthorResponse(
        val userId: UserId,
        val name: String,
    )

    companion object {
        fun from(result: ProjectDetailResult): ProjectDetailResponse =
            ProjectDetailResponse(
                projectId = result.projectId,
                title = result.title,
                description = result.description,
                githubUrl = result.githubUrl,
                deployUrl = result.deployUrl,
                thumbnailImageUrl = result.thumbnailImageUrl,
                techStacks = result.techStacks,
                members = result.members.map { m ->
                    MemberResponse(
                        userId = m.userId,
                        name = m.name,
                        profileImageUrl = m.profileImageUrl,
                        trackName = m.trackName,
                        position = m.position,
                    )
                },
                viewCount = result.viewCount,
                reactionCount = result.reactionCount,
                reactedByMe = result.reactedByMe,
                author = AuthorResponse(result.author.userId, result.author.name),
                createdAt = result.createdAt,
                modifiedAt = result.modifiedAt,
            )
    }
}
