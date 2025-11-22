package kr.co.wground.like.application

import kr.co.wground.like.application.dto.LikeCreateDto
import kr.co.wground.like.infra.LikeJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Service
class LikeService(
    private val likeRepository: LikeJpaRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun likePost(dto: LikeCreateDto) {
        try {
            transactionTemplate.execute {
                likeRepository.save(dto.toDomain())
            }
        } catch (_: DataIntegrityViolationException) {
            // 이미 좋아요를 누른 경우, DB의 유니크 제약 조건으로 인해 예외가 발생할 수 있습니다.
            // 이 예외는 정상적인 흐름으로 간주하고 무시합니다.
        }
    }
}
