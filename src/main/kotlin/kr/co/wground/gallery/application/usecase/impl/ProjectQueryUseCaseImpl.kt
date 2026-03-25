package kr.co.wground.gallery.application.usecase.impl

import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.application.repository.ProjectRepository.DetailRow
import kr.co.wground.gallery.application.repository.ProjectRepository.SummaryRow
import kr.co.wground.gallery.application.usecase.ProjectQueryUseCase
import kr.co.wground.gallery.application.usecase.query.GetProjectDetailQuery
import kr.co.wground.gallery.application.usecase.query.GetProjectListQuery
import kr.co.wground.gallery.application.usecase.result.ProjectDetailResult
import kr.co.wground.gallery.application.usecase.result.ProjectSummaryResult
import kr.co.wground.gallery.application.usecase.result.TrackFilterResult
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.image.application.ImageStorageService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectQueryUseCaseImpl(
    private val projectRepository: ProjectRepository,
    private val imageStorageService: ImageStorageService,
) : ProjectQueryUseCase {

    @Transactional(readOnly = true)
    override fun getList(query: GetProjectListQuery): Page<ProjectSummaryResult> {
        val page = projectRepository.findPagedSummaries(query.trackId, query.keyword, query.toPageRequest())
        val likeStats = projectRepository.findReactStats(
            projectIds = page.content.map { it.projectId }.toSet(),
            userId = query.userId,
        )
        return page.map { it.toResult(likeStats[it.projectId] ?: ProjectRepository.ProjectReaction.EMPTY) }
    }

    @Transactional
    override fun getDetail(query: GetProjectDetailQuery): ProjectDetailResult {
        val row = projectRepository.findDetailById(query.projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        projectRepository.incrementViewCount(query.projectId)
        val reactStat = projectRepository.findReactStats(setOf(query.projectId), query.userId)
        return row.toResult(reactStat[query.projectId] ?: ProjectRepository.ProjectReaction.EMPTY)
    }

    @Transactional(readOnly = true)
    override fun getTrackFilters(): List<TrackFilterResult> =
        projectRepository.findUsedTrackFilters().map { TrackFilterResult(it.trackId, it.trackName) }

    // ── 매핑

    private fun SummaryRow.toResult(reactStats: ProjectRepository.ProjectReaction) = ProjectSummaryResult(
        projectId = projectId,
        title = title,
        thumbnailImageUrl = imageStorageService.toUrl(thumbnailImagePath),
        trackNames = trackNames,
        techStacks = techStacks.splitToTechStackList(),
        memberCount = memberCount,
        viewCount = viewCount,
        reactionCount = reactStats.reactionCount,
        reactedByMe = reactStats.reactedByMe,
        createdAt = createdAt,
    )

    private fun DetailRow.toResult(reactStats: ProjectRepository.ProjectReaction) = ProjectDetailResult(
        projectId = projectId,
        title = title,
        description = description,
        githubUrl = githubUrl,
        deployUrl = deployUrl,
        thumbnailImageUrl = imageStorageService.toUrl(thumbnailImagePath),
        techStacks = techStacks.splitToTechStackList(),
        members = members.map { m ->
            ProjectDetailResult.MemberInfo(
                userId = m.userId,
                name = m.name,
                profileImageUrl = m.profileImageUrl,
                trackName = m.trackName,
                position = m.position,
            )
        },
        viewCount = viewCount + 1,
        reactionCount = reactStats.reactionCount,
        reactedByMe = reactStats.reactedByMe,
        author = ProjectDetailResult.AuthorInfo(userId = authorId, name = authorName),
        createdAt = createdAt,
        modifiedAt = modifiedAt,
    )

    private fun String.splitToTechStackList() =
        split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
