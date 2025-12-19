package kr.co.wground.reaction.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.reaction.application.dto.ReactionDto
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.ReactionJpaRepository
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.post.infra.PostRepository
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Service
class ReactionService(
    private val reactionRepository: ReactionJpaRepository,
    private val postRepository: PostRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    fun changeReaction(dto: ReactionDto) {
        validatePostExists(dto)
        // To Do: reaction target 에 따라 분기하도록 변경해야 함
        // To Do: reaction type 의 경우 필드 중 하나로 들어감
//        when (dto.action) {
//            ReactionType.LIKED -> react(dto)
//            ReactionType.UNLIKED -> undo(dto)
//        }
    }

    private fun validatePostExists(dto: ReactionDto) {
        postRepository.findByIdOrNull(dto.postId)
            ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)
    }

    private fun react(dto: ReactionDto) {
        runIdempotently {
            reactionRepository.save(dto.toDomain())
        }
    }

    private fun undo(dto: ReactionDto) {
        runIdempotently {
            reactionRepository.deleteByUserIdAndPostId(dto.userId, dto.postId)
        }
    }

    private fun runIdempotently(action: () -> Unit) {
        try {
            transactionTemplate.execute { action() }
        } catch (e: DataIntegrityViolationException) {
            if (!e.isDuplicateReactionViolation()) {
                throw e
            }
        }
    }

    private fun DataIntegrityViolationException.isDuplicateReactionViolation(): Boolean {
        val constraint = cause as? ConstraintViolationException
        return constraint?.constraintName == "reaction_uk"
    }
}
