package kr.co.wground.gallery.application.repository

import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface ProjectRepository {
    fun save(project: Project): Project
    fun findById(id: ProjectId): Project?
    fun findPagedSummaries(trackId: TrackId?, keyword: String?, pageable: Pageable): Page<SummaryRow>
    fun findDetailById(projectId: ProjectId): DetailRow?
    fun incrementViewCount(projectId: ProjectId)
    fun findUsedTrackFilters(): List<TrackItem>
    fun findReactStats(projectIds: Set<ProjectId>, userId: UserId): Map<ProjectId, ProjectReaction>

    data class SummaryRow(
        val projectId: ProjectId,
        val title: String,
        val thumbnailImagePath: String,
        val techStacks: String,
        val viewCount: Int,
        val createdAt: LocalDateTime,
        val memberCount: Long,
        val trackNames: List<String>,
    )

    data class DetailRow(
        val projectId: ProjectId,
        val title: String,
        val description: String,
        val githubUrl: String,
        val deployUrl: String?,
        val thumbnailImagePath: String,
        val techStacks: String,
        val viewCount: Int,
        val authorId: UserId,
        val authorName: String,
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime,
        val members: List<MemberInfo>,
    ) {
        data class MemberInfo(
            val userId: UserId,
            val name: String,
            val profileImageUrl: String,
            val trackName: String,
            val position: Position,
        )
    }

    data class TrackItem(
        val trackId: TrackId,
        val trackName: String,
    )

    data class ProjectReaction(
        val reactionCount: Int,
        val reactedByMe: Boolean,
    ) {
        companion object {
            val EMPTY = ProjectReaction(0, false)
        }
    }
}
