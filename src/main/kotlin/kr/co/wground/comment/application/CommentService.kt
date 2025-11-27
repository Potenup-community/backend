package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
) {
    @Transactional
    fun write(dto: CommentCreateDto): Long {
        validateParentId(dto)
        val comment = Comment.create(
            dto.writerId.value,
            dto.postId,
            dto.parentId,
            dto.content,
        )
        return commentRepository.save(comment).id
    }

    private fun validateParentId(dto: CommentCreateDto) {
        dto.parentId?.let { parentId ->
            val parentComment = commentRepository.findById(parentId)
                .orElseThrow { BusinessException(CommentErrorCode.COMMENT_PARENT_ID_NOT_FOUND) }
            if (!parentComment.isParent()) {
                throw BusinessException(CommentErrorCode.COMMENT_REPLY_NOT_ALLOWED)
            }
        }
    }

    @Transactional
    fun update(dto: CommentUpdateDto, writerId: CurrentUserId) {
        val comment = commentRepository.findById(dto.commentId)
            .orElseThrow { BusinessException(CommentErrorCode.COMMENT_NOT_FOUND) }
        validateWriter(comment, writerId)
        dto.content?.let {
            comment.updateContent(it)
        }
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
