package kr.co.wground.like.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.like.application.dto.LikeDto
import kr.co.wground.like.infra.LikeJpaRepository
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.post.infra.PostRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Service
class LikeService(
    private val likeRepository: LikeJpaRepository,
    private val postRepository: PostRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    fun changeLike(dto: LikeDto) {
        postRepository.findByIdOrNull(dto.postId) ?: throw BusinessException(PostErrorCode.NOT_FOUND_POST)

        if (dto.liked) {
            like(dto)
        } else {
            unlike(dto)
        }
    }

    private fun like(dto: LikeDto) {
        runIdempotently {
            likeRepository.save(dto.toDomain())
        }
    }

    private fun unlike(dto: LikeDto) {
        runIdempotently {
            likeRepository.deleteByUserIdAndPostId(dto.userId, dto.postId)
        }
    }

    private fun runIdempotently(action: () -> Unit) {
        try {
            transactionTemplate.execute { action() }
        } catch (_: DataIntegrityViolationException) {
            // no-op
        }
    }
}
