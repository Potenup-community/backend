package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.post.infra.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun write(dto: CommentCreateDto): Long {
        validateExistTargetPost(dto)
        validateParentId(dto)
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
        dto.content?.let {
            comment.updateContent(it)
        }
    }

    @Transactional
    fun delete(id: CommentId, writerId: CurrentUserId) {
        val comment = findByCommentId(id)
        validateWriter(comment, writerId)
        comment.deleteContent();
    }

    private fun validateExistTargetPost(dto: CommentCreateDto) {
        postRepository.findById(dto.postId)
            .orElseThrow { BusinessException(CommentErrorCode.TARGET_POST_IS_NOT_FOUND) }
    }

    private fun validateParentId(dto: CommentCreateDto) {
        dto.parentId?.let { parentId ->
            val parentComment = commentRepository.findById(parentId)
                .orElseThrow { BusinessException(CommentErrorCode.COMMENT_PARENT_ID_NOT_FOUND) }
            if (parentComment.isParent()) {
                throw BusinessException(CommentErrorCode.COMMENT_REPLY_NOT_ALLOWED)
            }
        }
    }

    private fun findByCommentId(id: CommentId): Comment {
        return commentRepository.findById(id)
            .orElseThrow { BusinessException(CommentErrorCode.COMMENT_NOT_FOUND) }
    }

    private fun validateWriter(
        comment: Comment,
        writerId: CurrentUserId
    ) {
        if (comment.writerId != writerId.value) {
            throw BusinessException(CommentErrorCode.COMMENT_NOT_WRITER)
        }
    }
}
