package kr.co.wground.reaction.infra.querydsl

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.domain.Post
import kr.co.wground.reaction.infra.querydsl.CustomPostReactionRepositoryImpl.PostReactionStatsRow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomPostReactionRepository {
    fun fetchPostReactionStatsRows(postIds: Set<PostId>, userId: UserId): List<PostReactionStatsRow>
    fun findAllLikedByUser(userId: UserId,pageable: Pageable): Slice<Post>
}
