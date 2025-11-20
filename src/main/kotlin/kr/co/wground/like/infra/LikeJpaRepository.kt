package kr.co.wground.like.infra

import kr.co.wground.like.domain.Like
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface LikeJpaRepository : JpaRepository<Like, Long> {
    fun existsByUserIdAndPostId(userId: UserId, postId: PostId): Boolean
}
