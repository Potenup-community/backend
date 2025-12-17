package kr.co.wground.comment.infra

import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.PostId
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long>, CommentRepositoryCustom {
    fun findByPostId(postId: PostId): List<Comment>
}
