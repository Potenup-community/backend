package kr.co.wground.reaction.infra.querydsl

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.infra.querydsl.CommentReactionQuerydslRepository.CommentReactionStatsRow

interface CustomCommentReactionRepository {
    fun fetchCommentReactionStatsRows(commentIds: Set<CommentId>, userId: UserId): List<CommentReactionStatsRow>
}
