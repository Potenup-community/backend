package kr.co.wground.post.infra

import kr.co.wground.post.domain.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PostRepository: JpaRepository<Post, Long>, CustomPostRepository {
    fun findAllByCreatedAtAfterAndDeletedAtIsNull(createdAt: LocalDateTime): List<Post>
}
