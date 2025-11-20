package kr.co.wground.post.infra

import kr.co.wground.post.domain.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository: JpaRepository<Post, Long> {
}
