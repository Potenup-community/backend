package kr.co.wground.like.application

import kr.co.wground.like.application.dto.LikeCreateDto
import kr.co.wground.like.infra.LikeJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Service
class LikeService(
    private val likeRepository: LikeJpaRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    fun likePost(dto: LikeCreateDto) {
        runIdempotently {
            likeRepository.save(dto.toDomain())
        }
    }

    fun runIdempotently(action: () -> Unit) {
        try {
            transactionTemplate.execute { action() }
        } catch (_: DataIntegrityViolationException) {
            // no-op
        }
    }
}
