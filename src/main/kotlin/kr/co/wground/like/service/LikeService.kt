package kr.co.wground.like.service

import kr.co.wground.api.exception.BusinessException
import kr.co.wground.like.domain.Like
import kr.co.wground.like.exception.LikeErrorCode
import kr.co.wground.like.repository.LikeJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeJpaRepository
) {

    @Transactional
    fun likePost(userId: Long, postId: Long) {
        validateAlreadyLiked(userId, postId)

        val like = Like(
            userId = userId,
            postId = postId
        )

        likeRepository.save(like)
    }

    private fun validateAlreadyLiked(userId: Long, postId: Long) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BusinessException(LikeErrorCode.ALREADY_LIKED)
        }
    }
}
