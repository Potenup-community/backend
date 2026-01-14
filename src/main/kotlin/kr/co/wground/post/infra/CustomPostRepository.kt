package kr.co.wground.post.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomPostRepository {
    fun findAllByPredicate(predicate: GetPostSummaryPredicate): Slice<Post>
    fun findAllLikedByUser(userId: UserId, pageable: Pageable): Slice<Post>
}
