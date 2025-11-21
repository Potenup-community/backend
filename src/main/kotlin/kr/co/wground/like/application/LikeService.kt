package kr.co.wground.like.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.like.domain.Like
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId
import kr.co.wground.like.exception.LikeErrorCode
import kr.co.wground.like.infra.LikeJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeJpaRepository
) {

    @Transactional
    fun likePost(userId: UserId, postId: PostId) {
        validateAlreadyLiked(userId, postId)

        val like = Like(
            userId = userId,
            postId = postId
        )

        likeRepository.save(like)
    }

    private fun validateAlreadyLiked(userId: UserId, postId: PostId) {
        likeRepository.existsByUserIdAndPostId(userId, postId)
            .takeIf { it }?.let { throw BusinessException(LikeErrorCode.ALREADY_LIKED) }
    }
}
