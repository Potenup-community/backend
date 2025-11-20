package kr.co.wground.like.infra

import kr.co.wground.like.domain.Like
import org.springframework.data.jpa.repository.JpaRepository

interface LikeJpaRepository : JpaRepository<Like, Long> {
    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean
}
