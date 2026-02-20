package kr.co.wground.gallery.infra.persistence.jpa

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.gallery.domain.model.QProject
import kr.co.wground.gallery.domain.model.QProjectMember
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.QProjectReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.track.domain.QTrack
import kr.co.wground.user.domain.QUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProjectRepositoryImpl(
    private val jpaRepository: ProjectJpaRepository,
    private val queryFactory: JPAQueryFactory,
) : ProjectRepository {
    private val qProject = QProject.project
    private val qMember = QProjectMember.projectMember
    private val qUser = QUser.user
    private val qTrack = QTrack.track
    private val qProjectReaction = QProjectReaction.projectReaction

    override fun save(project: Project): Project = jpaRepository.save(project)
    override fun findById(id: ProjectId): Project? = jpaRepository.findByIdOrNull(id)
    override fun incrementViewCount(projectId: ProjectId) = jpaRepository.incrementViewCount(projectId)

    override fun findPagedSummaries(
        trackId: TrackId?,
        keyword: String?,
        pageable: Pageable,
    ): Page<ProjectRepository.SummaryRow> {
        val predicate = buildPredicate(trackId, keyword)

        val total = queryFactory
            .select(qProject.id.countDistinct())
            .from(qProject)
            .leftJoin(qMember).on(qMember.project.id.eq(qProject.id))
            .leftJoin(qUser).on(qUser.userId.eq(qMember.userId))
            .leftJoin(qTrack).on(qTrack.trackId.eq(qUser.trackId))
            .where(predicate)
            .fetchOne() ?: 0L

        if (total == 0L) return PageImpl(emptyList(), pageable, 0L)

        val projectIds = queryFactory
            .selectDistinct(qProject.id, qProject.createdAt, qProject.viewCount)
            .from(qProject)
            .leftJoin(qMember).on(qMember.project.id.eq(qProject.id))
            .leftJoin(qUser).on(qUser.userId.eq(qMember.userId))
            .leftJoin(qTrack).on(qTrack.trackId.eq(qUser.trackId))
            .where(predicate)
            .orderBy(resolveOrder(pageable))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
            .map { requireNotNull(it.get(qProject.id)) }

        if (projectIds.isEmpty()) return PageImpl(emptyList(), pageable, total)

        val grouped = queryFactory
            .select(
                qProject.id, qProject.content.title, qProject.thumbnailImagePath,
                qProject.techStacks, qProject.viewCount, qProject.createdAt,
                qMember.userId, qTrack.trackName,
            )
            .from(qProject)
            .leftJoin(qMember).on(qMember.project.id.eq(qProject.id))
            .leftJoin(qUser).on(qUser.userId.eq(qMember.userId))
            .leftJoin(qTrack).on(qTrack.trackId.eq(qUser.trackId))
            .where(qProject.id.`in`(projectIds))
            .fetch()
            .groupBy { requireNotNull(it.get(qProject.id)) }

        val content = projectIds.mapNotNull { pid ->
            grouped[pid]?.let { pidRows ->
                val first = pidRows.first()
                ProjectRepository.SummaryRow(
                    projectId = pid,
                    title = requireNotNull(first.get(qProject.content.title)),
                    thumbnailImagePath = requireNotNull(first.get(qProject.thumbnailImagePath)),
                    techStacks = requireNotNull(first.get(qProject.techStacks)),
                    viewCount = requireNotNull(first.get(qProject.viewCount)),
                    createdAt = requireNotNull(first.get(qProject.createdAt)),
                    memberCount = pidRows.mapNotNull { it.get(qMember.userId) }.distinct().size.toLong(),
                    trackNames = pidRows.mapNotNull { it.get(qTrack.trackName) }.distinct(),
                )
            }
        }

        return PageImpl(content, pageable, total)
    }

    override fun findDetailById(projectId: ProjectId): ProjectRepository.DetailRow? {
        val rows = queryFactory
            .select(
                qProject.id, qProject.content.title, qProject.content.description,
                qProject.githubUrl, qProject.deployUrl, qProject.thumbnailImagePath,
                qProject.techStacks, qProject.viewCount, qProject.authorId,
                qProject.createdAt, qProject.modifiedAt,
                qMember.userId, qMember.position,
                qUser.name, qUser.userProfile.imageUrl, qUser.userProfile.currentFileName,
                qTrack.trackName,
            )
            .from(qProject)
            .leftJoin(qMember).on(qMember.project.id.eq(qProject.id))
            .leftJoin(qUser).on(qUser.userId.eq(qMember.userId))
            .leftJoin(qTrack).on(qTrack.trackId.eq(qUser.trackId))
            .where(qProject.id.eq(projectId), qProject.deletedAt.isNull)
            .fetch()
            .takeIf { it.isNotEmpty() } ?: return null

        val first = rows.first()
        val authorId = requireNotNull(first.get(qProject.authorId))

        val members = rows.mapNotNull { row ->
            val userId = row.get(qMember.userId) ?: return@mapNotNull null
            val imageUrl = row.get(qUser.userProfile.imageUrl).orEmpty()
            val fileName = row.get(qUser.userProfile.currentFileName)
            ProjectRepository.DetailRow.MemberInfo(
                userId = userId,
                name = row.get(qUser.name).orEmpty(),
                profileImageUrl = if (!fileName.isNullOrBlank()) "$imageUrl/$fileName" else imageUrl,
                trackName = row.get(qTrack.trackName).orEmpty(),
                position = requireNotNull(row.get(qMember.position)),
            )
        }

        return ProjectRepository.DetailRow(
            projectId = requireNotNull(first.get(qProject.id)),
            title = requireNotNull(first.get(qProject.content.title)),
            description = requireNotNull(first.get(qProject.content.description)),
            githubUrl = requireNotNull(first.get(qProject.githubUrl)),
            deployUrl = first.get(qProject.deployUrl),
            thumbnailImagePath = requireNotNull(first.get(qProject.thumbnailImagePath)),
            techStacks = requireNotNull(first.get(qProject.techStacks)),
            viewCount = requireNotNull(first.get(qProject.viewCount)),
            authorId = authorId,
            authorName = rows.firstOrNull { it.get(qMember.userId) == authorId }?.get(qUser.name).orEmpty(),
            createdAt = requireNotNull(first.get(qProject.createdAt)),
            modifiedAt = requireNotNull(first.get(qProject.modifiedAt)),
            members = members,
        )
    }

    override fun findUsedTrackFilters(): List<ProjectRepository.TrackItem> =
        queryFactory
            .select(qTrack.trackId, qTrack.trackName)
            .distinct()
            .from(qProject)
            .join(qMember).on(qMember.project.id.eq(qProject.id))
            .join(qUser).on(qUser.userId.eq(qMember.userId))
            .join(qTrack).on(qTrack.trackId.eq(qUser.trackId))
            .where(qProject.deletedAt.isNull)
            .orderBy(qTrack.trackId.asc())
            .fetch()
            .map {
                ProjectRepository.TrackItem(
                    trackId = requireNotNull(it.get(qTrack.trackId)),
                    trackName = requireNotNull(it.get(qTrack.trackName)),
                )
            }

    override fun findReactStats(
        projectIds: Set<ProjectId>,
        userId: UserId
    ): Map<ProjectId, ProjectRepository.ProjectReaction> {
        if (projectIds.isEmpty()) return emptyMap()

        val countExpr = qProjectReaction.id.count()
        val reactedByMeMax = CaseBuilder()
            .`when`(qProjectReaction.userId.eq(userId)).then(1)
            .otherwise(0)
            .max()

        return queryFactory
            .select(qProjectReaction.projectId, countExpr, reactedByMeMax)
            .from(qProjectReaction)
            .where(
                qProjectReaction.projectId.`in`(projectIds),
                qProjectReaction.reactionType.eq(ReactionType.LIKE),
            )
            .groupBy(qProjectReaction.projectId)
            .fetch()
            .associate { tuple ->
                val projectId = requireNotNull(tuple.get(qProjectReaction.projectId))
                val reactionCount = requireNotNull(tuple.get(countExpr))
                val reactedByMe = requireNotNull(tuple.get(reactedByMeMax))
                projectId to ProjectRepository.ProjectReaction(
                    reactionCount = reactionCount.toInt(),
                    reactedByMe = reactedByMe > 0,
                )
            }
    }

    // ── 헬퍼

    private fun buildPredicate(trackId: TrackId?, keyword: String?) = BooleanBuilder().apply {
        and(qProject.deletedAt.isNull)
        trackId?.let { and(qTrack.trackId.eq(it)) }
        keyword?.takeIf { it.isNotBlank() }?.let {
            and(
                qProject.content.title.containsIgnoreCase(it)
                    .or(qProject.content.description.containsIgnoreCase(it))
            )
        }
    }

    private fun resolveOrder(pageable: Pageable): OrderSpecifier<*> {
        val sort = pageable.sort.firstOrNull()
        val direction = if (sort?.isAscending == true) Order.ASC else Order.DESC
        return when (sort?.property) {
            "viewCount" -> OrderSpecifier(direction, qProject.viewCount)
            else -> OrderSpecifier(direction, qProject.createdAt)
        }
    }
}
