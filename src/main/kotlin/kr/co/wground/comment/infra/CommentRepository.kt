package kr.co.wground.comment.infra

import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.PostId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long>, CommentRepositoryCustom {
    fun findByPostIdAndParentIdIsNull(postId: PostId, pageable: Pageable): Slice<Comment>
    fun findByPostIdAndParentIdIn(postId: PostId, parentIds: List<Long>): List<Comment>
}
