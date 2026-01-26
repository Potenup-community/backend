package kr.co.wground.post.infra

import kr.co.wground.post.domain.Post
import kr.co.wground.post.infra.dto.PostNavigationDto
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import org.springframework.data.domain.Slice
import java.time.LocalDateTime

interface CustomPostRepository {
    fun findAllByPredicate(predicate: GetPostSummaryPredicate): Slice<Post>
    fun findIdsOfPreviousAndNext(currentPostId: Long, currentCreatedAt: LocalDateTime): PostNavigationDto
}
