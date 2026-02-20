package kr.co.wground.reaction.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.reaction.application.dto.ProjectReactionStats
import kr.co.wground.reaction.exception.ReactionErrorCode
import kr.co.wground.reaction.infra.jpa.PostReactionJpaRepository
import kr.co.wground.reaction.application.dto.PostReactionStats
import kr.co.wground.reaction.application.dto.ReactionSummary
import kr.co.wground.reaction.application.dto.LikedCommentDto
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.jpa.CommentReactionJpaRepository
import kr.co.wground.reaction.infra.jpa.ProjectReactionJpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReactionQueryService(
    private val postReactionJpaRepository: PostReactionJpaRepository,
    private val postRepository: PostRepository,
    private val commentReactionJpaRepository: CommentReactionJpaRepository,
    private val projectReactionJpaRepository: ProjectReactionJpaRepository,
) {

    fun getPostReactionStats(postId: PostId, userId: UserId): PostReactionStats {
        validatePostExistence(postId)

        val reactions = postReactionJpaRepository.findPostReactionsByPostId(postId)
        val groupedByType = reactions.groupBy { it.reactionType }
        val userReactionTypes = reactions
            .filter { it.userId == userId }
            .map { it.reactionType }
            .toSet()

        val summaries = groupedByType.mapValues { (reactionType, reactionList) ->
            ReactionSummary(
                count = reactionList.size,
                reactedByMe = reactionType in userReactionTypes
            )
        }

        return PostReactionStats(
            postId = postId,
            totalCount = reactions.size,
            summaries = summaries
        )
    }

    fun getPostReactionStats(postIds: Set<PostId>, userId: UserId): Map<PostId, PostReactionStats> {

        if (postIds.isEmpty()) {
            return emptyMap()
        }

        // To Do: 나중에 매직 넘버 뺄 생각입니다.
        if (postIds.size > 50) {
            throw BusinessException(ReactionErrorCode.TOO_LARGE_POST_ID_SET)
        }

        // postIds 집합에 속한 각 postId 에 해당하는 게시글들의 실존 여부 검증은 따로 하지 않을 생각임
        // 안 해도 될 듯? 없으면 어차피 결과 안 나갈거니까

        val rowsFetched = postReactionJpaRepository.fetchPostReactionStatsRows(postIds, userId)

        val rowsByPostId = rowsFetched.groupBy { it.postId }

        return postIds
            .asSequence()
            .mapNotNull { postId ->
                val postRows = rowsByPostId[postId].orEmpty()

                // postId 에 해당하는 반응 정보가 없는 경우
                val totalCount = postRows.sumOf { it.count }
                if (totalCount == 0L) {
                    return@mapNotNull null
                }

                val summaries = postRows.associate { r ->
                    r.reactionType to ReactionSummary(
                        count = r.count.toInt(),
                        reactedByMe = r.reactedByMe
                    )
                }

                postId to PostReactionStats(
                    postId,
                    totalCount = totalCount.toInt(),
                    summaries = summaries
                )
            }
            .toMap()
    }

    fun getCommentReactionStats(commentIds: Set<CommentId>, userId: Long): Map<CommentId, CommentReactionStats> {

        if (commentIds.isEmpty()) {
            return emptyMap()
        }

        // To Do: 나중에 매직 넘버 뺄 생각입니다.
        if (commentIds.size > 50) {
            throw BusinessException(ReactionErrorCode.TOO_LARGE_COMMENT_ID_SET)
        }

        // commentIds 집합에 속한 각 commentId 에 해당하는 댓글들의 실존 여부 검증은 따로 하지 않을 생각임
        // 안 해도 될 듯? 없으면 어차피 결과 안 나갈거니까

        val rowsFetched = commentReactionJpaRepository.fetchCommentReactionStatsRows(commentIds, userId)

        val rowsByPostId = rowsFetched.groupBy { it.commentId }

        return commentIds
            .asSequence()
            .mapNotNull { commentId ->
                val commentRows = rowsByPostId[commentId].orEmpty()

                // commentId 에 해당하는 반응 정보가 없는 경우
                val totalCount = commentRows.sumOf { it.count }
                if (totalCount == 0L) {
                    return@mapNotNull null
                }

                val summaries = commentRows.associate { r ->
                    r.reactionType to ReactionSummary(
                        count = r.count.toInt(),
                        reactedByMe = r.reactedByMe
                    )
                }

                commentId to CommentReactionStats(
                    commentId,
                    totalCount = totalCount.toInt(),
                    summaries = summaries
                )
            }
            .toMap()
    }

    fun getProjectReactionStats(projectIds: Set<ProjectId>, userId: UserId): Map<ProjectId, ProjectReactionStats> {
        if (projectIds.isEmpty()) return emptyMap()

        if (projectIds.size > 50) {
            throw BusinessException(ReactionErrorCode.TOO_LARGE_PROJECT_ID_SET)
        }

        val rowsFetched = projectReactionJpaRepository.fetchProjectReactionStatsRows(projectIds, userId)
        val rowsByProjectId = rowsFetched.groupBy { it.projectId }

        return projectIds
            .asSequence()
            .mapNotNull { projectId ->
                val projectRows = rowsByProjectId[projectId].orEmpty()
                val totalCount = projectRows.sumOf { it.count }
                if (totalCount == 0L) return@mapNotNull null

                val summaries = projectRows.associate { r ->
                    r.reactionType to ReactionSummary(count = r.count.toInt(), reactedByMe = r.reactedByMe)
                }

                projectId to ProjectReactionStats(projectId, totalCount = totalCount.toInt(), summaries = summaries)
            }
            .toMap()
    }

    fun getLikedComments(userId: UserId, pageable: Pageable): Slice<LikedCommentDto> {
        return commentReactionJpaRepository
            .findByUserIdAndReactionType(userId, ReactionType.LIKE, pageable)
            .map { LikedCommentDto(commentId = it.commentId, likedAt = it.createdAt) }
    }

    // validation --------------------

    fun validatePostExistence(postId: PostId) {
        if (!postRepository.existsById(postId)) {
            throw BusinessException(ReactionErrorCode.POST_NOT_FOUND)
        }
    }
}
