package kr.co.wground.reaction.infra.querydsl

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.infra.querydsl.PostReactionQuerydslRepository.PostReactionStatsRow

interface CustomPostReactionRepository {
    fun fetchPostReactionStatsRows(postIds: Set<PostId>, userId: UserId): List<PostReactionStatsRow>
}
