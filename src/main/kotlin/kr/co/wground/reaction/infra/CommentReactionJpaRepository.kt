package kr.co.wground.reaction.infra

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.CommentReaction
import org.springframework.data.jpa.repository.JpaRepository

interface CommentReactionJpaRepository : JpaRepository<CommentReaction, Long> {
    fun deleteByUserIdAndCommentId(userId: UserId, commentId: CommentId) : Long
}