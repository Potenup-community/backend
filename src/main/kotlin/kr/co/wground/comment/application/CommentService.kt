package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
) {
    @Transactional
    fun write(dto: CommentDto): Long {
        validateParentId(dto)
        val comment = Comment.create(
            dto.writerId,
            dto.postId,
            dto.parentId,
            dto.content,
        )
        return commentRepository.save(comment).id
    }

    private fun validateParentId(dto: CommentDto) {
        dto.parentId?.let { parentId ->
            val parentComment = commentRepository.findById(parentId)
                .orElseThrow { BusinessException(CommentErrorCode.COMMENT_PARENT_ID_NOT_FOUND) }
            if (!parentComment.isParent()) {
                throw BusinessException(CommentErrorCode.COMMENT_REPLY_NOT_ALLOWED)
            }
        }
    }
}
