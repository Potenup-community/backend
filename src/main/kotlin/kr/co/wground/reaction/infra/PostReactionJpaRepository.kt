package kr.co.wground.reaction.infra

import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface PostReactionJpaRepository : JpaRepository<PostReaction, Long> {
    fun deleteByUserIdAndPostId(userId: UserId, postId: PostId): Long
}
