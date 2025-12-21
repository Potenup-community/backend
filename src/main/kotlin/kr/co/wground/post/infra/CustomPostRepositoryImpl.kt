package kr.co.wground.post.infra

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.QPost
import kr.co.wground.post.domain.QPost.post
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl

class CustomPostRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory): CustomPostRepository {
    override fun findAllByPageable(pageable: Pageable): SliceImpl<Post> {
        val content = jpaQueryFactory.selectFrom(post)
            .distinct()
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
}
