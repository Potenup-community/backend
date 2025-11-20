package kr.co.wground.api.post.infra

import kr.co.wground.api.post.domain.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository: JpaRepository<Post, Long> {
}