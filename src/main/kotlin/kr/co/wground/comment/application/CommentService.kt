package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.comment.application.dto.LikedCommentSummaryDto
import kr.co.wground.comment.application.dto.MyCommentSummaryDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.ReactionQueryService
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val reactionQueryService: ReactionQueryService,
) {
    @Transactional
    fun write(dto: CommentCreateDto): Long {
        validateExistTargetPost(dto.postId)
        validateParentId(dto.parentId)
        val comment = Comment.create(
            dto.writerId,
            dto.postId,
            dto.parentId,
            dto.content,
        )
        return commentRepository.save(comment).id
    }

    @Transactional
    fun update(dto: CommentUpdateDto, writerId: CurrentUserId) {
        val comment = findByCommentId(dto.commentId)
        validateWriter(comment, writerId)
        dto.content?.let(comment::updateContent)
    }

    @Transactional
    fun delete(id: CommentId, writerId: CurrentUserId) {
        val comment = findByCommentId(id)
        validateWriter(comment, writerId)
        comment.deleteContent()
    }

    @Transactional(readOnly = true)
    fun getCommentsByPost(
        postId: PostId,
        userId: CurrentUserId
    ): List<CommentSummaryDto> {
        validateExistTargetPost(postId)

        val allComments = commentRepository.findAllByPostId(postId)
        val usersById = loadUsersByComments(allComments)
        val reactionStatsByCommentId = fetchReactionCounts(allComments, userId.value)

        return groupCommentsWithReplies(allComments, usersById, reactionStatsByCommentId)
    }

    @Transactional(readOnly = true)
    fun getCommentsByMe(
        userId: CurrentUserId,
        pageable: Pageable,
    ): Slice<MyCommentSummaryDto> {
        val comments = commentRepository.findAllByWriterId(userId.value, pageable)
        if (comments.isEmpty) {
            return SliceImpl(emptyList(), pageable, comments.hasNext())
        }

        val usersById = loadUsersByComments(comments.content)
        val reactionStatsById = fetchReactionCounts(comments.content, userId.value)

        val summaries = comments.content.map { comment ->
            MyCommentSummaryDto.from(
                comment = comment,
                author = usersById[comment.writerId],
                reactionStats = reactionStatsById[comment.id],
            )
        }

        return SliceImpl(summaries, pageable, comments.hasNext())
    }

    @Transactional(readOnly = true)
    fun getLikedComments(userId: CurrentUserId, pageable: Pageable): Slice<LikedCommentSummaryDto> {
        val likedReactions = reactionQueryService.getLikedComments(userId.value, pageable)
        if (likedReactions.isEmpty) {
            return SliceImpl(emptyList(), pageable, likedReactions.hasNext())
        }

        val likedCommentIds = likedReactions.content.map { it.commentId }
        val commentsById = commentRepository
            .findAllById(likedCommentIds.toSet())
            .associateBy { it.id }

        val comments = commentsById.values.toList()
        val reactionStatsById = fetchReactionCounts(comments, userId.value)

        val summaries = likedReactions.content.mapNotNull { likedReaction ->
            val comment = commentsById[likedReaction.commentId] ?: return@mapNotNull null

            LikedCommentSummaryDto.from(
                comment = comment,
                reactionStats = reactionStatsById[comment.id],
                likedAt = likedReaction.likedAt,
            )
        }

        return SliceImpl(summaries, pageable, likedReactions.hasNext())
    }

    fun validateMentionUserIds(mentionUserIds: List<Long>?, writerId: UserId): List<Long> {
        if (mentionUserIds.isNullOrEmpty()) return emptyList()

        val users = userRepository.findByUserIdIn(mentionUserIds)

        val foundIds = users.map { it.userId }.toSet()
        val notFoundIds = mentionUserIds.filter { it !in foundIds }
        if (notFoundIds.isNotEmpty()) {
            throw BusinessException(CommentErrorCode.MENTION_USER_NOT_FOUND)
        }

        return users
            .filter { it.userId != writerId && it.status == UserStatus.ACTIVE }
            .map { it.userId }
    }

    private fun validateExistTargetPost(postId: PostId) {
        postRepository.findByIdOrNull(postId) ?: throw BusinessException(CommentErrorCode.TARGET_POST_IS_NOT_FOUND)
    }

    private fun validateParentId(parentId: CommentId?) {
        parentId?.let {
            val parentComment = commentRepository.findByIdOrNull(it)
                ?: throw BusinessException(CommentErrorCode.COMMENT_PARENT_ID_NOT_FOUND)
            if (!parentComment.isParent())
                throw BusinessException(CommentErrorCode.COMMENT_REPLY_NOT_ALLOWED)
        }
    }

    private fun findByCommentId(id: CommentId): Comment {
        return commentRepository.findByIdOrNull(id) ?: throw BusinessException(CommentErrorCode.COMMENT_NOT_FOUND)
    }

    private fun validateWriter(
        comment: Comment,
        writerId: CurrentUserId
    ) {
        if (comment.writerId != writerId.value)
            throw BusinessException(CommentErrorCode.COMMENT_NOT_WRITER)
    }

    private fun loadUsersByComments(
        comments: List<Comment>
    ): Map<UserId, UserDisplayInfoDto> {
        val writerIds = comments.map { it.writerId }.toSet()
        return userRepository.findUserDisplayInfos(writerIds.toList())
    }

    private fun groupCommentsWithReplies(
        comments: List<Comment>,
        authorsById: Map<UserId, UserDisplayInfoDto>,
        reactionStatsById: Map<CommentId, CommentReactionStats>,
    ): List<CommentSummaryDto> {
        val groupedByParent = comments
            .sortedWith(compareBy<Comment> { it.createdAt }.thenBy { it.id })
            .groupBy { it.parentId }

        fun toSummary(comment: Comment): CommentSummaryDto {
            val replies = groupedByParent[comment.id].orEmpty().map { toSummary(it) }

            return CommentSummaryDto.from(
                comment = comment,
                author = authorsById[comment.writerId],
                reactionStats = reactionStatsById[comment.id],
                replies = replies,
            )
        }

        return groupedByParent[null].orEmpty().map { toSummary(it) }
    }

    private fun fetchReactionCounts(comments: List<Comment>, userId: UserId): Map<CommentId, CommentReactionStats> {
        val commentIds = comments.map { it.id }.toSet()
        if (commentIds.isEmpty()) return emptyMap()
        // 크기 제한으로 배치 조회
        return commentIds
            .chunked(50)
            .flatMap { chunk ->
                reactionQueryService.getCommentReactionStats(chunk.toSet(), userId).entries
            }
            .associate { (commentId, stats) ->
                commentId to stats
            }
    }
}
