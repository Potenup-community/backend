package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.comment.application.dto.CommentUpdateDto
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
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
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
        pageable: Pageable,
        userId: CurrentUserId
    ): Slice<CommentSummaryDto> {
        validateExistTargetPost(postId)

        val parentSlice = commentRepository.findByPostIdAndParentIdIsNull(postId, pageable)
        if (parentSlice.isEmpty) return SliceImpl(emptyList(), pageable, parentSlice.hasNext())

        val parentIds = parentSlice.content.map { it.id }
        val replies = commentRepository.findByPostIdAndParentIdIn(postId, parentIds)

        val allComments = parentSlice.content + replies
        val usersById = loadUsersByComments(allComments)
        val reactionCountByCommentId = fetchReactionCounts(allComments, userId.value)

        val tree = CommentSummaryTreeBuilder.from(allComments, usersById, reactionCountByCommentId).build()

        return SliceImpl(tree, pageable, parentSlice.hasNext())
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
    ): Map<UserId, User> {
        val writerIds = comments.map { it.writerId }.toSet()
        return userRepository.findAllById(writerIds).associateBy { it.userId }
    }

    private fun fetchReactionCounts(comments: List<Comment>, userId: UserId): Map<CommentId, Int> {
        val commentIds = comments.map { it.id }.toSet()
        if (commentIds.isEmpty()) return emptyMap()
        // 크기 제한으로 배치 조회
        return commentIds
            .chunked(50)
            .flatMap { chunk ->
                reactionQueryService.getCommentReactionStats(chunk.toSet(), userId).entries
            }
            .associate { (commentId, stats) ->
                commentId to stats.totalCount
            }
    }
}
