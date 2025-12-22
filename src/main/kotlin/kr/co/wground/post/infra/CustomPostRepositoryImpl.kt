package kr.co.wground.post.infra

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.QPost.post
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import org.springframework.data.domain.SliceImpl

class CustomPostRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory): CustomPostRepository {
    override fun findAllByPredicate(predicate: GetPostSummaryPredicate): SliceImpl<Post> {
        val pageable = predicate.pageable

        val content = jpaQueryFactory.selectFrom(post)
            .where(eqTopic(predicate.topic))
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong() + 1)
            .fetch()

        val hasNext = content.size > pageable.pageSize

        return SliceImpl(
            content.take(pageable.pageSize),
            pageable,
            hasNext
        )
    }

    private fun eqTopic(topic: Topic?) = topic?.let { post.topic.eq(it) }
}
