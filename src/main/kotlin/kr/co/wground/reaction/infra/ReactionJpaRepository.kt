package kr.co.wground.reaction.infra

import kr.co.wground.reaction.domain.Reaction
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionJpaRepository : JpaRepository<Reaction, Long> {
    fun deleteByUserIdAndPostId(userId: UserId, postId: PostId): Long
}
