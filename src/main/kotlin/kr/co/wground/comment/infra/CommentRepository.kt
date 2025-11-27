package kr.co.wground.comment.infra

import kr.co.wground.comment.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
}
